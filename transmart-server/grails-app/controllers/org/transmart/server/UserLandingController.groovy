package org.transmart.server

import org.springframework.web.servlet.support.RequestContextUtils
import org.transmart.searchapp.AccessLog

class UserLandingController {
    /**
     * Dependency injection for the springSecurityService.
     */
    def springSecurityService
    def messageSource

    private String getUserLandingPath() {
        grailsApplication.config.with {
            com.recomdata.defaults.landing ?: ui.tabs.browse.hide ? '/datasetExplorer' : '/RWG'
        }
    }

    def index = {
        def skip_disclaimer = grailsApplication.config.com.recomdata?.skipdisclaimer ?: false;
        if (skip_disclaimer) {
            if (springSecurityService?.currentUser?.changePassword) {
                flash.message = messageSource.getMessage('changePassword', new Objects[0], RequestContextUtils.getLocale(request))
                redirect(controller: 'changeMyPassword')
            } else {
                redirect(uri: userLandingPath)
            }
        } else {
            redirect(uri: '/userLanding/disclaimer.gsp')
        }
    }
    def agree = {
        if (springSecurityService?.currentUser?.changePassword) {
            flash.message = messageSource.getMessage('changePassword', new Objects[0], RequestContextUtils.getLocale(request))
            redirect(controller: 'changeMyPassword')
        } else {
            redirect(uri: userLandingPath)
        }
    }

    def disagree = {
        redirect(uri: '/logout')
    }

    def checkHeartBeat = {
        render(text: "OK")
    }

}
