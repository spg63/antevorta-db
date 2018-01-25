/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dataAssociator.reddit

import edu.gbcg.dbInteraction.dbSelector.BaseMapper
import edu.gbcg.dbInteraction.dbSelector.RSMapper
import edu.gbcg.dbInteraction.dbSelector.reddit.comments.RedditComSelector

class RedditComOrganizer {
    private val _submission: RSMapper

    constructor(submission: RSMapper){
        _submission = submission
    }

    fun getAllCommentsFromSubmission(submission: RSMapper = BaseMapper()): List<RSMapper> {
        // If it's a BaseMapper then we use the RSMapper from the c'tor
        val sub = if(submission is BaseMapper) _submission else submission

        // Determine what the link id is for this submission
        val linkIDPartial = sub.getString("pid")

        // Prepend the comment pid tag
        val linkID = "t3_" + linkIDPartial

        // Get a comment selector
        val comSelector = RedditComSelector()

        // Select all comments who have the same parent
        return comSelector.selectAllWhereColumnEquals("link_id", linkID)
    }
}