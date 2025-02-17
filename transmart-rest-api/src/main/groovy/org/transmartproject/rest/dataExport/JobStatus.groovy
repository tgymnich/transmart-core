package org.transmartproject.rest.dataExport

import groovy.transform.CompileStatic

@CompileStatic
enum JobStatus {

    CREATED ("Created"),
    STARTED ("Started"),
    GATHERING_DATA ("Gathering Data"),
    CANCELLED ("Cancelled"),
    COMPLETED ("Completed"),
    ERROR ("Error")
    
    final String value
    
    JobStatus(String value) { this.value = value }
    
    String toString() { value }
    String getKey() { name() }
}
