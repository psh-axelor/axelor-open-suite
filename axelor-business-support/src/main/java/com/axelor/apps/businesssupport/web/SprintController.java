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
package com.axelor.apps.businesssupport.web;

import com.axelor.apps.project.db.Sprint;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class SprintController {

  public void targetVersionDomain(ActionRequest request, ActionResponse response) {

    Sprint sprint = request.getContext().asType(Sprint.class);

    String domain =
        sprint != null && sprint.getProject() != null
            ? sprint.getProject().getId() + " member of self.projectSet"
            : "self.id in (0)";

    response.setAttr("targetVersion", "domain", domain);
  }
}