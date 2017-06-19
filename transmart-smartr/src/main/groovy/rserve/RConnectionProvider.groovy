package rserve

import grails.core.GrailsApplication
import org.rosuda.REngine.Rserve.RConnection
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class RConnectionProvider {

    @Autowired
    GrailsApplication grailsApplication

    RConnection get() {
        def conn = new RConnection(grailsApplication.config.RModules.host ?: '127.0.0.1',
                                   grailsApplication.config.RModules.port ?: 6311)
        conn.eval("options(bitmapType='cairo')")
        conn
    }
}
