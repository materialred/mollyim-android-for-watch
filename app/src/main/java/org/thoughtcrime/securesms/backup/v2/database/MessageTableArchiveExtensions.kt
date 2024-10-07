/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.thoughtcrime.securesms.backup.v2.database

import org.signal.core.util.SqlUtil
import org.signal.core.util.select
import org.thoughtcrime.securesms.backup.v2.ImportState
import org.thoughtcrime.securesms.backup.v2.exporters.ChatItemArchiveExporter
import org.thoughtcrime.securesms.backup.v2.importer.ChatItemArchiveImporter
import org.thoughtcrime.securesms.database.MessageTable
import org.thoughtcrime.securesms.database.MessageTypes
import org.thoughtcrime.securesms.database.SignalDatabase
import java.util.concurrent.TimeUnit

private const val COLUMN_BASE_TYPE = "base_type"

fun MessageTable.getMessagesForBackup(db: SignalDatabase, backupTime: Long, mediaBackupEnabled: Boolean): ChatItemArchiveExporter {
  val cursor = readableDatabase
    .select(
      MessageTable.ID,
      MessageTable.DATE_SENT,
      MessageTable.DATE_RECEIVED,
      MessageTable.DATE_SERVER,
      MessageTable.TYPE,
      MessageTable.THREAD_ID,
      MessageTable.BODY,
      MessageTable.MESSAGE_RANGES,
      MessageTable.FROM_RECIPIENT_ID,
      MessageTable.TO_RECIPIENT_ID,
      MessageTable.EXPIRES_IN,
      MessageTable.EXPIRE_STARTED,
      MessageTable.REMOTE_DELETED,
      MessageTable.UNIDENTIFIED,
      MessageTable.LINK_PREVIEWS,
      MessageTable.SHARED_CONTACTS,
      MessageTable.QUOTE_ID,
      MessageTable.QUOTE_AUTHOR,
      MessageTable.QUOTE_BODY,
      MessageTable.QUOTE_MISSING,
      MessageTable.QUOTE_BODY_RANGES,
      MessageTable.QUOTE_TYPE,
      MessageTable.ORIGINAL_MESSAGE_ID,
      MessageTable.LATEST_REVISION_ID,
      MessageTable.HAS_DELIVERY_RECEIPT,
      MessageTable.HAS_READ_RECEIPT,
      MessageTable.VIEWED_COLUMN,
      MessageTable.RECEIPT_TIMESTAMP,
      MessageTable.READ,
      MessageTable.NETWORK_FAILURES,
      MessageTable.MISMATCHED_IDENTITIES,
      "${MessageTable.TYPE} & ${MessageTypes.BASE_TYPE_MASK} AS $COLUMN_BASE_TYPE",
      MessageTable.MESSAGE_EXTRAS
    )
    .from(MessageTable.TABLE_NAME)
    .where(
      """
      (
        ${MessageTable.EXPIRE_STARTED} = 0 
        OR 
        (${MessageTable.EXPIRES_IN} > 0 AND (${MessageTable.EXPIRE_STARTED} + ${MessageTable.EXPIRES_IN}) > $backupTime + ${TimeUnit.DAYS.toMillis(1)})
      )
      AND ${MessageTable.STORY_TYPE} = 0
      """
    )
    .orderBy("${MessageTable.DATE_RECEIVED} ASC")
    .run()

  return ChatItemArchiveExporter(db, cursor, 100, mediaBackupEnabled)
}

fun MessageTable.createChatItemInserter(importState: ImportState): ChatItemArchiveImporter {
  return ChatItemArchiveImporter(writableDatabase, importState, 100)
}

fun MessageTable.clearAllDataForBackupRestore() {
  writableDatabase.delete(MessageTable.TABLE_NAME, null, null)
  SqlUtil.resetAutoIncrementValue(writableDatabase, MessageTable.TABLE_NAME)
}