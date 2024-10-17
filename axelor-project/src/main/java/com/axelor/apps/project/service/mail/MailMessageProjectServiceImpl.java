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
package com.axelor.apps.project.service.mail;

import com.axelor.apps.base.db.MailMessageFile;
import com.axelor.apps.base.service.mail.MailMessageServiceImpl;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.mail.db.MailMessage;
import com.axelor.mail.db.repo.MailMessageRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class MailMessageProjectServiceImpl extends MailMessageServiceImpl
    implements MailMessageProjectService {

  @Inject
  public MailMessageProjectServiceImpl(MailMessageRepository mailMessageRepo) {

    super(mailMessageRepo);
  }

  @Override
  public MailMessage computeMailMessage(ProjectTask projectTask, MailMessage message) {

    message.setAuthor(projectTask.getUpdatedBy());
    message.setRelatedId(projectTask.getId());
    message.setRelatedModel(ProjectTask.class.getName());
    message.setIsPublicNote(
        message.getId() != null ? message.getIsPublicNote() : projectTask.getIsPublicNote());
    message.setNote(message.getId() != null ? message.getNote() : projectTask.getNote());

    List<MailMessageFile> mailMessageFileList = projectTask.getMailMessageFileList();

    if (CollectionUtils.isNotEmpty(mailMessageFileList)) {

      for (MailMessageFile mailMessageFile : mailMessageFileList) {
        message.addMailMessageFileListItem(mailMessageFile);
      }
    }

    projectTask.setNote("");
    projectTask.clearMailMessageFileList();

    return message;
  }

  @Override
  @Transactional
  public void createMailMessageWithOnlyAttachment(ProjectTask projectTask) {

    mailMessageRepo.save(computeMailMessage(projectTask, new MailMessage()));
  }
}
