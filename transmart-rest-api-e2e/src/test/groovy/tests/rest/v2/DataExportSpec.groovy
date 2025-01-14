package tests.rest.v2

import annotations.RequiresStudy
import base.RESTSpec
import groovy.util.logging.Slf4j
import org.transmartproject.core.multidimquery.ErrorResponse
import org.transmartproject.core.multidimquery.export.ExportJob

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

import static base.ContentTypeFor.JSON
import static base.ContentTypeFor.ZIP
import static base.RestHelper.toObject
import static config.Config.*
import static tests.rest.Operator.AND
import static tests.rest.Operator.EQUALS
import static tests.rest.ValueType.STRING
import static tests.rest.constraints.*

@Slf4j
class DataExportSpec extends RESTSpec {

    def "create a new dataExport job"() {
        def name = null
        def request = [
                path      : "$PATH_DATA_EXPORT/job",
                acceptType: JSON,
        ]

        when: "Export job name is NOT specified"
        def response = post(request) as Map
        def responseData = toObject(response.exportJob, ExportJob)
        def id = responseData.id

        then: "A new job with default name is returned"
        assert id != null
        assert responseData.jobName == id.toString()
        assert responseData.jobStatus == "Created"
        assert responseData.jobStatusTime != null
        assert responseData.userId == getUsername(DEFAULT_USER)
        assert responseData.viewerUrl == null

        when: "Export job name is specified"
        name = 'test_job_name' + id
        request.path = "$PATH_DATA_EXPORT/job"
        request.query = [name: name]
        response = post(request) as Map
        responseData = toObject(response.exportJob, ExportJob)

        then: "A new job with specified name is returned "
        assert responseData.id != null
        assert responseData.jobName == name
        assert responseData.jobStatus == "Created"
        assert responseData.jobStatusTime != null
        assert responseData.userId == getUsername(DEFAULT_USER)
        assert responseData.viewerUrl == null
    }

    @RequiresStudy(TUMOR_NORMAL_SAMPLES_ID)
    def "get data formats for patientSet"() {
        when: "I check data_formats for the constraint"
        def getDataFormatsResponse = post([
                path      : "$PATH_DATA_EXPORT/data_formats",
                acceptType: JSON,
                body      : [
                        constraint: [
                                type  : ModifierConstraint, path: "\\Public Studies\\TUMOR_NORMAL_SAMPLES\\Sample Type\\",
                                values: [type: ValueConstraint, valueType: STRING, operator: EQUALS, value: "Tumor"]
                        ],
                ],
        ])

        then: "I get data formats for both clinical and highDim types"
        assert getDataFormatsResponse != null
        assert getDataFormatsResponse.dataFormats.contains("clinical")
    }

    @RequiresStudy(TUMOR_NORMAL_SAMPLES_ID)
    def "run data export without 'Export' permission"() {
        def patientSetRequest = [
                path      : PATH_PATIENT_SET,
                acceptType: JSON,
                query     : [name: 'export_test_set'],
                body      : [
                        type  : ModifierConstraint, path: "\\Public Studies\\TUMOR_NORMAL_SAMPLES\\Sample Type\\",
                        values: [type: ValueConstraint, valueType: STRING, operator: EQUALS, value: "Tumor"]
                ],
                user      : ADMIN_USER,
                statusCode: 201
        ]
        def createPatientSetResponse = post(patientSetRequest) as Map
        def patientSetId = createPatientSetResponse.id

        def newJobRequest = [
                path      : "$PATH_DATA_EXPORT/job",
                acceptType: JSON,
                user      : DEFAULT_USER
        ]
        def newJobResponse = post(newJobRequest) as Map
        def jobId = toObject(newJobResponse.exportJob, ExportJob).id

        when: "I run a newly created job asynchronously"
        def response = post([
                path      : "$PATH_DATA_EXPORT/$jobId/run",
                body      : [
                        constraint: [type: PatientSetConstraint, patientSetId: patientSetId],
                        elements  :
                                [[
                                         dataType: 'clinical',
                                         format  : 'TSV',
                                         dataView : 'surveyTable'
                                 ]],
                ],
                user      : DEFAULT_USER,
                statusCode: 403
        ]) as Map
        def responseData = toObject(response, ErrorResponse)

       then:
       responseData.message.startsWith("Access denied")
       responseData.message.contains(patientSetId.toString())
    }

    @RequiresStudy(TUMOR_NORMAL_SAMPLES_ID)
    def "run data export"() {
        def newJobRequest = [
                path      : "$PATH_DATA_EXPORT/job",
                acceptType: JSON,
                user      : ADMIN_USER
        ]
        def newJobResponse = post(newJobRequest) as Map
        def jobData = toObject(newJobResponse.exportJob, ExportJob)
        def jobId = jobData.id
        def jobName = jobData.jobName

        when: "I run a newly created job asynchronously"
        def runResponse = post([
                path      : "$PATH_DATA_EXPORT/$jobId/run",
                body      : [
                        constraint: [
                                type  : ModifierConstraint, path: "\\Public Studies\\TUMOR_NORMAL_SAMPLES\\Sample Type\\",
                                values: [type: ValueConstraint, valueType: STRING, operator: EQUALS, value: "Tumor"]
                        ],
                        elements  : [[
                                             dataType: 'clinical',
                                             format  : 'TSV',
                                             dataView : 'surveyTable'
                                     ]],
                ],
                acceptType: JSON,
                user      : ADMIN_USER
        ]) as Map
        jobData = toObject(runResponse.exportJob, ExportJob)

        then: "Job instance with status: 'Started' is returned"
        assert jobData.id == jobId
        assert jobData.jobStatus == 'Started'
        assert jobData.jobStatusTime != null
        assert jobData.userId == getUsername(ADMIN_USER)
        assert jobData.viewerUrl == null

        when: "Check the status of the job"
        int maxAttemptNumber = 10 // max number of status check attempts
        def statusRequest = [
                path      : "$PATH_DATA_EXPORT/$jobId/status",
                acceptType: JSON,
                user      : ADMIN_USER
        ]
        def statusResponse = get(statusRequest) as Map
        jobData = toObject(statusResponse.exportJob, ExportJob)

        then: "Returned status is 'Completed'"
        def status = jobData.jobStatus

        // waiting for async process to end (increase number of attempts if needed)
        for (int attempNum = 0; status != 'Completed' && attempNum < maxAttemptNumber; attempNum++) {
            sleep(500)
            statusResponse = get(statusRequest) as Map
            jobData = toObject(statusResponse.exportJob, ExportJob)
            status = jobData.jobStatus
        }

        assert status == 'Completed'

        when: "Try to download the file"
        def downloadRequest = [
                path      : "$PATH_DATA_EXPORT/$jobId/download",
                acceptType: ZIP,
                user      : ADMIN_USER
        ]
        def downloadResponse = get(downloadRequest)

        then: "ZipStream is returned"
        assert downloadResponse != null


        expect: 'users who did not request file has to get 404 code'
        get([
                path      : "$PATH_DATA_EXPORT/$jobId/download",
                acceptType: ZIP,
                user      : DEFAULT_USER,
                statusCode: 404
        ])

    }

    def "list all dataExport jobs for user"() {
        def createJobRequest = [
                path      : "$PATH_DATA_EXPORT/job",
                acceptType: JSON,
        ]
        def createJobResponse = post(createJobRequest) as Map
        def jobData = toObject(createJobResponse.exportJob, ExportJob)

        when: "I try to fetch list of all export jobs"
        def getJobsResponse = get([
                path      : "$PATH_DATA_EXPORT/jobs",
                acceptType: JSON,
        ]) as Map
        def jobsData = getJobsResponse.exportJobs as List<Map>
        def jobs = jobsData.collect { Map job -> toObject(job, ExportJob) }

        then: "The list of all data export job, including the newly created one is returned"
        jobs != null
        !jobs.empty
        jobs.contains(jobData)
    }

    def "get supported file formats"() {
        def request = [
                path      : "$PATH_DATA_EXPORT/file_formats",
                acceptType: JSON,
                query: [dataView : 'dataTable']
        ]

        when: "I request all supported fields"
        def responseData = get(request)

        then:
        "I get a list of fields containing the supported formats"
        responseData.fileFormats == ['TSV']
    }

    @RequiresStudy(TUMOR_NORMAL_SAMPLES_ID)
    def "run data export with either id or constraint parameter only"() {
        def newJobRequest = [
                path      : "$PATH_DATA_EXPORT/job",
                acceptType: JSON,
        ]
        def newJobResponse = post(newJobRequest) as Map
        def jobData = toObject(newJobResponse.exportJob, ExportJob)
        def jobId = jobData.id

        when: "I run a newly created job without id nor constraint parameter supplied."
        def runResponse = post([
                path      : "$PATH_DATA_EXPORT/$jobId/run",
                body      : [
                        elements: [[
                                           dataType: 'clinical',
                                           format  : 'TSV',
                                           dataView : 'surveyTable'
                                   ]],
                ],
                acceptType: JSON,
                statusCode: 400,
        ]) as Map

        then: "I get the error."
        runResponse.message == '1 error(s): constraint: may not be null'
    }

    @RequiresStudy(EHR_ID)
    def "run data export using a constraint"() {
        when:
        def downloadResponse = runTypicalExport([
                constraint: [type: ConceptConstraint,
                             path: "\\Public Studies\\EHR\\Vital Signs\\Heart Rate\\"],
                elements  : [[
                                     dataType: 'clinical',
                                     format  : 'TSV',
                                     dataView: 'dataTable'
                             ]],
                tableConfig: [rowDimensions: ['patient'], columnDimensions: ['concept']]
        ])

        then:
        assert downloadResponse.downloadResponse != null
        def filesLineNumbers = getFilesLineNumbers(downloadResponse.downloadResponse as byte[])
        filesLineNumbers['metadata.json']
        filesLineNumbers['data.tsv']
        filesLineNumbers['patient.tsv']
        filesLineNumbers['concept.tsv']
    }

    @RequiresStudy(SURVEY1_ID)
    def "export survey to tsv file format"() {
        when:
        def downloadResponse = runTypicalExport([
                constraint: [type   : StudyNameConstraint,
                             studyId: SURVEY1_ID],
                elements  : [[
                                     dataType: 'clinical',
                                     format  : 'TSV',
                                     dataView : 'surveyTable'
                             ]],
                includeMeasurementDateColumns: true,
        ])

        then:
        assert downloadResponse.downloadResponse != null
        def filesLineNumbers = getFilesLineNumbers(downloadResponse.downloadResponse as byte[])
        filesLineNumbers.size() == 3
        filesLineNumbers['data.tsv'] == 16
        filesLineNumbers['variables.tsv'] == 18
        filesLineNumbers['value_labels.tsv'] == 6

    }

    @RequiresStudy(SURVEY1_ID)
    def "export survey to tsv file format without dates"() {
        when:
        def downloadResponse = runTypicalExport([
                constraint: [type   : StudyNameConstraint,
                             studyId: SURVEY1_ID],
                elements  : [[
                                     dataType: 'clinical',
                                     format  : 'TSV',
                                     dataView : 'surveyTable'
                             ]],
                includeMeasurementDateColumns: false,
        ])
        String fileName = downloadResponse.jobName

        then:
        assert downloadResponse.downloadResponse != null
        def filesLineNumbers = getFilesLineNumbers(downloadResponse.downloadResponse as byte[])
        filesLineNumbers.size() == 3
        filesLineNumbers['data.tsv'] == 16
        filesLineNumbers['variables.tsv'] == 10
        filesLineNumbers['value_labels.tsv'] == 6

    }

    @RequiresStudy(SURVEY1_ID)
    def "export survey to spss file format"() {
        when:
        def downloadResponse = runTypicalExport([
                constraint: [type   : StudyNameConstraint,
                             studyId: SURVEY1_ID],
                elements  : [[
                                     dataType: 'clinical',
                                     format  : 'SPSS',
                                     dataView : 'surveyTable'
                             ]],
                includeMeasurementDateColumns: true,
        ])
        String fileName = downloadResponse.jobName

        then:
        assert downloadResponse.downloadResponse != null
        def filesLineNumbers = getFilesLineNumbers(downloadResponse.downloadResponse as byte[])
        // Number of files depends on pspp being installed. If so, a file spss/data.sav is added as well.
        filesLineNumbers.size() == 2 || filesLineNumbers.size() == 3
        filesLineNumbers["${fileName}_spss/data.tsv"] == 15
        filesLineNumbers["${fileName}_spss/data.sps"] == 102
        if (filesLineNumbers.size() == 3) {
            assert filesLineNumbers["${fileName}_spss/${fileName}.sav"] > 0
        }
    }

    @RequiresStudy(SURVEY1_ID)
    def 'parallel export survey to spss file format'() {
        when: 'I make a patientset with everyone included in SURVEY1'
        def request = [
                path      : PATH_PATIENT_SET,
                acceptType: JSON,
                query     : [name: 'test_set'],
                body      : [type   : StudyNameConstraint,
                             studyId: SURVEY1_ID],
                statusCode: 201
        ]
        def responseData = post(request) as Map

        then: 'I get a patientset with 14 patients'
        responseData.id != null
        responseData.setSize == 14

        when: 'I export data for the patient set for data from the study with multiple workers'
        put([
                path      : PATH_CONFIG,
                acceptType: JSON,
                body      : [numberOfWorkers: 2, patientSetChunkSize: 5],
                user      : ADMIN_USER,
                statusCode: 200
        ])
        def downloadResponse = runTypicalExport([
                constraint: [type: AND,
                             args: [
                                 [type   : PatientSetConstraint,
                                  patientSetId: responseData.id as Long],
                                 [type   : StudyNameConstraint,
                                  studyId: SURVEY1_ID]
                            ]],
                elements  : [[
                                     dataType: 'clinical',
                                     format  : 'SPSS',
                                     dataView : 'surveyTable'
                             ]],
                includeMeasurementDateColumns: true,
        ])
        def fileName = downloadResponse.jobName

        then: 'the result contains the expected files with expected number of rows'
        assert downloadResponse.downloadResponse != null
        def filesLineNumbers = getFilesLineNumbers(downloadResponse.downloadResponse as byte[])
        // Number of files depends on pspp being installed. If so, a file ${fileName}_spss/${fileName}.sav is added as well.
        filesLineNumbers.size() == 2 || filesLineNumbers.size() == 3
        filesLineNumbers["${fileName}_spss/data.tsv"] == 15
        filesLineNumbers["${fileName}_spss/data.sps"] == 102
        if (filesLineNumbers.size() == 3) {
            assert filesLineNumbers["${fileName}_spss/${fileName}.sav"] > 0
        }
    }

    @RequiresStudy(CATEGORICAL_VALUES_ID)
    def "export non conventional study to tsv file format"() {
        when:
        def downloadResponse = runTypicalExport([
                constraint: [type   : StudyNameConstraint,
                             studyId: CATEGORICAL_VALUES_ID],
                elements  : [[
                                     dataType: 'clinical',
                                     format  : 'TSV',
                                     dataView : 'surveyTable'
                             ]],
        ])

        then:
        assert downloadResponse.downloadResponse != null
        def filesLineNumbers = getFilesLineNumbers(downloadResponse.downloadResponse as byte[])
        filesLineNumbers.size() == 2
        filesLineNumbers['data.tsv'] == 4
        filesLineNumbers['variables.tsv'] == 6
    }

    @RequiresStudy(CATEGORICAL_VALUES_ID)
    def "export not conventional to spss file format"() {
        when:
        def downloadResponse = runTypicalExport([
                constraint: [type   : StudyNameConstraint,
                             studyId: CATEGORICAL_VALUES_ID],
                elements  : [[
                                     dataType: 'clinical',
                                     format  : 'SPSS',
                                     dataView : 'surveyTable'
                             ]],
        ])
        def fileName = downloadResponse.jobName

        then:
        assert downloadResponse.downloadResponse != null
        def filesLineNumbers = getFilesLineNumbers(downloadResponse.downloadResponse as byte[])
        // Number of files depends on pspp being installed. If so, a file ${fileName}_spss/${fileName}.sav is added as well.
        filesLineNumbers.size() == 2 || filesLineNumbers.size() == 3
        filesLineNumbers["${fileName}_spss/data.tsv"] == 4
        filesLineNumbers["${fileName}_spss/data.sps"] == 42
        if (filesLineNumbers.size() == 3) {
            assert filesLineNumbers["${fileName}_spss/${fileName}.sav"] > 0
        }
    }

    def "get supported file formats for survey table"() {
        def request = [
                path      : "$PATH_DATA_EXPORT/file_formats",
                acceptType: JSON,
                query     : [dataView : 'surveyTable']
        ]

        when: "I request all supported fields"
        def responseData = get(request)

        then:
        "I get a list of fields containing the supported formats"
        assert 'TSV' in responseData.fileFormats
        assert 'SPSS' in responseData.fileFormats
    }

    private Map<String, Object> runTypicalExport(Map body) {
        def newJobRequest = [
                path      : "$PATH_DATA_EXPORT/job",
                acceptType: JSON,
        ]
        def newJobResponse = post(newJobRequest) as Map
        def jobData = toObject(newJobResponse.exportJob, ExportJob)
        def jobId = jobData.id
        def jobName = jobData.jobName

        def runResponse = post([
                path      : "$PATH_DATA_EXPORT/$jobId/run",
                body      : body,
                acceptType: JSON,
        ]) as Map
        jobData = toObject(runResponse.exportJob, ExportJob)

        assert jobData.id == jobId
        assert jobData.jobStatus == 'Started'
        assert jobData.jobStatusTime != null
        assert jobData.userId == getUsername(DEFAULT_USER)
        assert jobData.viewerUrl == null

        int maxAttemptNumber = 50 // max number of status check attempts
        def statusRequest = [
                path      : "$PATH_DATA_EXPORT/${jobId}/status",
                acceptType: JSON,
        ]
        def statusResponse = get(statusRequest) as Map
        jobData = toObject(statusResponse.exportJob, ExportJob)
        def status = jobData.jobStatus

        // waiting for async process to end (increase number of attempts if needed)
        for (int attempNum = 0; status != 'Completed' && attempNum < maxAttemptNumber; attempNum++) {
            sleep(500)
            statusResponse = get(statusRequest) as Map
            jobData = toObject(statusResponse.exportJob, ExportJob)
            status = jobData.jobStatus
        }

        assert status == 'Completed'

        def downloadRequest = [
                path      : "$PATH_DATA_EXPORT/${jobId}/download",
                acceptType: ZIP,
        ]
        def downloadResponse = get(downloadRequest)

        return [jobName: jobId, downloadResponse: downloadResponse]
    }

    @RequiresStudy(CATEGORICAL_VALUES_ID)
    def "cancel job"() {
        def patientSetRequest = [
                path      : PATH_PATIENT_SET,
                acceptType: JSON,
                query     : [name: 'test_cancelation_job'],
                body      : [type  : StudyNameConstraint, studyId: CATEGORICAL_VALUES_ID],
                statusCode: 201
        ]
        def createPatientSetResponse = post(patientSetRequest)
        def patientSetId = createPatientSetResponse.id

        def newJobRequest = [
                path      : "$PATH_DATA_EXPORT/job",
                acceptType: JSON,
        ]
        def newJobResponse = post(newJobRequest) as Map
        def jobData = toObject(newJobResponse.exportJob, ExportJob)
        def jobId = jobData.id

        when: 'I cancel the job'
        post([
                path      : "$PATH_DATA_EXPORT/$jobId/cancel",
                statusCode: 200
        ])

        def statusRequest = [
                path      : "$PATH_DATA_EXPORT/$jobId",
                acceptType: JSON,
        ]
        def statusResponse = get(statusRequest) as Map
        jobData = toObject(statusResponse.exportJob, ExportJob)

        then: "Returned status is 'Canceled'"
        jobData.jobStatus == 'Cancelled'
    }

    @RequiresStudy(CATEGORICAL_VALUES_ID)
    def "delete job"() {
        def patientSetRequest = [
                path      : PATH_PATIENT_SET,
                acceptType: JSON,
                query     : [name: 'test_cancelation_job'],
                body      : [type  : StudyNameConstraint, studyId: CATEGORICAL_VALUES_ID],
                statusCode: 201
        ]
        def createPatientSetResponse = post(patientSetRequest)
        def patientSetId = createPatientSetResponse.id

        def newJobRequest = [
                path      : "$PATH_DATA_EXPORT/job",
                acceptType: JSON,
        ]
        def newJobResponse = post(newJobRequest) as Map
        def jobData = toObject(newJobResponse.exportJob, ExportJob)
        def jobId = jobData.id

        when: "I run a newly created job asynchronously"
        def responseData = post([
                path      : "$PATH_DATA_EXPORT/$jobId/run",
                body      : [
                        constraint: [type: PatientSetConstraint, patientSetId: patientSetId],
                        elements  :
                                [[
                                         dataType: 'clinical',
                                         format  : 'TSV',
                                         dataView: 'surveyTable'
                                 ]],
                ],
        ]) as Map
        jobData = toObject(responseData.exportJob, ExportJob)

        then: "Job instance with status: 'Started' is returned"
        jobData.id == jobId
        jobData.jobStatus == 'Started'

        when:
        delete([
                path      : "$PATH_DATA_EXPORT/$jobId",
                statusCode: 200
        ])

        then:
        get([
                path      : "$PATH_DATA_EXPORT/$jobId",
                acceptType: JSON,
                statusCode: 404
        ])
    }

    @RequiresStudy(ORACLE_1000_PATIENT_ID)
    def "export big study"() {
        def patientSetRequest = [
                path      : PATH_PATIENT_SET,
                acceptType: JSON,
                query     : [name: 'all_patients'],
                body      : [type: StudyNameConstraint, studyId: ORACLE_1000_PATIENT_ID],
                statusCode: 201
        ]
        def createPatientSetResponse = post(patientSetRequest)
        def patientSet = createPatientSetResponse

        when:
        def downloadResponse = runTypicalExport([
                constraint: [type      : PatientSetConstraint,
                             patientSetId: patientSet.id ],
                elements  : [[
                                     dataType: 'clinical',
                                     format  : 'TSV',
                                     dataView: 'surveyTable'
                             ]],
        ])

        then:
        assert downloadResponse != null
        def filesLineNumbers = getFilesLineNumbers(downloadResponse.downloadResponse as byte[])
        filesLineNumbers.size() == 2
        filesLineNumbers['data.tsv'] == patientSet.setSize + 1
        filesLineNumbers['variables.tsv'] == 100 + 1 /*header*/ + 1 //subj id
    }

    static private Map<String, Integer> getFilesLineNumbers(byte[] content) {
        Map<String, Integer> result = [:]
        def zipInputStream
        try {
            zipInputStream = new ZipInputStream(new ByteArrayInputStream(content))
            ZipEntry entry
            while (entry = zipInputStream.nextEntry) {
                def reader = new BufferedReader(new InputStreamReader(zipInputStream))
                Integer linesNumber = 0
                while (reader.readLine()) {
                    linesNumber += 1
                }
                result[entry.name] = linesNumber
            }
        } finally {
            if (zipInputStream) zipInputStream.close()
        }

        result
    }

}
