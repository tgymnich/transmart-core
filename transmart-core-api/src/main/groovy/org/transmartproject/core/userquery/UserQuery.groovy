package org.transmartproject.core.userquery

/**
 *  Stores patients and observations parts of queries
 *  @deprecated user queries related functionality has been moved to a gb-backend application
 */
@Deprecated
interface UserQuery {
    /**
     * Internal system identifier of the query.
     */
    Long getId()

    /**
     * The query name
     */
    String getName()

    void setName(String name)

    /**
     * The query username
     */
    String getUsername()

    /**
     * The patient selection part of the query in json format.
     */
    String getPatientsQuery()

    void setPatientsQuery(String patientsQuery)

    /**
     * The observation selection part of the query.
     */
    String getObservationsQuery()

    void setObservationsQuery(String observationsQuery)

    /**
     * The version of the API the query was intended for.
     */
    String getApiVersion()

    void setApiVersion(String apiVersion)

    /**
     * Flag to indicate if the user has bookmarked the query.
     */
    Boolean getBookmarked()

    void setBookmarked(Boolean bookmarked)

    /**
     * Flag to indicate if the user has subscribed for the query.
     */
    Boolean getSubscribed()

    void setSubscribed(Boolean subscribed)

    /**
     * Frequency of the subscription: daily or weekly
     */
    SubscriptionFrequency getSubscriptionFreq()

    void setSubscriptionFreq(SubscriptionFrequency subscriptionFreq)

    /**
     * Creation date and time of this query.
     */
    Date getCreateDate()

    /**
     * When this query was updated.
     */
    Date getUpdateDate()

    /**
     * Change the update date to now.
     */
    void updateUpdateDate()

    /**
     * Additional information about the query, e.g. a state of the data table
     */
    String getQueryBlob()

    void setQueryBlob(String queryBlob)
}
