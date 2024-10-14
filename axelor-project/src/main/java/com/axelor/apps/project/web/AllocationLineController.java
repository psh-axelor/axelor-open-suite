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
package com.axelor.apps.project.web;

import com.axelor.apps.project.db.AllocationLine;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.service.sprint.AllocationLineService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.math.BigDecimal;
import java.util.HashMap;

public class AllocationLineController {

  public void userDomain(ActionRequest request, ActionResponse response) {

    AllocationLine allocationLine = request.getContext().asType(AllocationLine.class);

    Sprint sprint = allocationLine.getSprint();

    String domain =
        sprint != null
            ? sprint.getProject().getId() + " member of self.projectSet"
            : "self.id in (0)";

    response.setAttr("user", "domain", domain);
  }

  public void computeAllocationLine(ActionRequest request, ActionResponse response) {

    AllocationLine allocationLine = request.getContext().asType(AllocationLine.class);

    HashMap<String, BigDecimal> valueMap =
        Beans.get(AllocationLineService.class).computeAllocationLine(allocationLine);

    response.setValue("$leaves", valueMap.get("leaves"));
    response.setValue("$plannedTime", valueMap.get("plannedTime"));
    response.setValue("$remainingTime", valueMap.get("remainingTime"));
  }
}
