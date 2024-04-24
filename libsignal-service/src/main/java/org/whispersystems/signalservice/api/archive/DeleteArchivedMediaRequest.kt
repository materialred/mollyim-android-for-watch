/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.signalservice.api.archive

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Delete media from the backup cdn.
 */
class DeleteArchivedMediaRequest(
  @JsonProperty("mediaToDelete") val mediaToDelete: List<ArchivedMediaObject>
) {
  class ArchivedMediaObject(
    @JsonProperty("cdn") val cdn: Int,
    @JsonProperty("mediaId") val mediaId: String
  )
}
