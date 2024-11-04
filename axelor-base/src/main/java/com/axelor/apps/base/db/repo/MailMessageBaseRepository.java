/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.db.repo;

import com.axelor.app.AppSettings;
import com.axelor.apps.base.db.MailMessageFile;
import com.axelor.mail.db.MailMessage;
import com.axelor.mail.db.repo.MailMessageRepository;
import com.axelor.meta.db.MetaFile;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

public class MailMessageBaseRepository extends MailMessageRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {

    MailMessage mailMessage = this.find((Long) json.get("id"));

    List<MailMessageFile> mailMessageFileList = mailMessage.getMailMessageFileList();

    StringBuilder sb = new StringBuilder();

    if (CollectionUtils.isNotEmpty(mailMessageFileList)) {
      String baseURL = AppSettings.get().getBaseURL();
      String urlFormat = "%s/ws/rest/com.axelor.meta.db.MetaFile/%d/content/download?v=%d";

      for (MailMessageFile mailMessageFile : mailMessageFileList) {
        MetaFile attachmentFile = mailMessageFile.getAttachmentFile();

        if (attachmentFile != null) {
          sb.append("<li> ")
              .append("<a href='")
              .append(
                  String.format(
                      urlFormat, baseURL, attachmentFile.getId(), attachmentFile.getVersion()))
              .append("'>")
              .append(attachmentFile.getFileName())
              .append("</a></li>");
        }
      }
    }

    json.put("$mailMessageFiles", sb.toString());

    return super.populate(json, context);
  }
}
