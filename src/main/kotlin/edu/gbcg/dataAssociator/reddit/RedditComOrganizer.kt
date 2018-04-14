/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dataAssociator.reddit

import edu.gbcg.dbInteraction.dbSelector.RSMapper
import edu.gbcg.dbInteraction.dbSelector.reddit.comments.RedditComSelector
import edu.gbcg.utils.TSL

@Suppress("ConvertSecondaryConstructorToPrimary")
class RedditComOrganizer {
    private val submission: RSMapper
    private val _logger = TSL.get()

    constructor(submission: RSMapper){
        this.submission = submission
    }

    @Deprecated("Aren't all returned mappers from a query basemappers? WTF SEAN!?")
    fun getAllCommentsFromSubmission(): List<RSMapper> {
        // Determine what the link id is for this submission
        val linkIDPartial = this.submission.getString("pid")

        // Prepend the comment pid tag
        val linkID = "t3_$linkIDPartial"

        // Get a comment selector
        val comSelector = RedditComSelector()

        // Select all comments who have the same parent
        return comSelector.selectAllWhereColumnEquals("link_id", linkID)
    }
}