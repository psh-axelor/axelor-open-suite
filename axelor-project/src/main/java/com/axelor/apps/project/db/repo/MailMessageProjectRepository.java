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
package com.axelor.apps.project.db.repo;

import com.axelor.apps.base.db.repo.MailMessageBaseRepository;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.service.mail.MailMessageProjectService;
import com.axelor.inject.Beans;
import com.axelor.mail.db.MailMessage;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class MailMessageProjectRepository extends MailMessageBaseRepository {

  @Override
  public MailMessage save(MailMessage entity) {

    if (entity.getRelatedModel().equals(ProjectTask.class.getName())) {
      ProjectTask projectTask = Beans.get(ProjectTaskRepository.class).find(entity.getRelatedId());
      entity = Beans.get(MailMessageProjectService.class).computeMailMessage(projectTask, entity);
    }

    return super.save(entity);
  }

  @Override
  public void remove(MailMessage entity) {

    List<MailMessage> mailMessagetList =
        all()
            .filter(
                "self.id != ?1 and self.relatedModel = ?2 and self.relatedId =?3",
                entity.getId(),
                entity.getRelatedModel(),
                entity.getRelatedId())
            .fetch();

    if (CollectionUtils.isNotEmpty(mailMessagetList)) {

      for (MailMessage mailMessage : mailMessagetList) {

        if (entity.equals(mailMessage.getParentMailMessage())) {
          mailMessage.setParentMailMessage(null);
        }

        save(mailMessage);
      }
    }

    super.remove(entity);
  }
}
