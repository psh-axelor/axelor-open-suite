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

import com.axelor.apps.project.db.AllocationPeriod;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.repo.AllocationPeriodRepository;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.service.sprint.SprintService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SprintController {

  @SuppressWarnings("unchecked")
  public void allocationPeriodDomain(ActionRequest request, ActionResponse response) {

    Object projectsContext = request.getContext().get("projects");

    Set<Project> projects = new HashSet<>();

    if (projectsContext != null) {
      ProjectRepository projectRepo = Beans.get(ProjectRepository.class);

      projects =
          ((List<LinkedHashMap<String, Object>>) projectsContext)
              .stream()
                  .map(project -> projectRepo.find(Long.valueOf(project.get("id").toString())))
                  .collect(Collectors.toSet());
    }

    response.setAttr(
        "allocationPeriods",
        "domain",
        Beans.get(SprintService.class).allocationPeriodDomain(projects));
  }

  @SuppressWarnings("unchecked")
  public void generateSprints(ActionRequest request, ActionResponse response) {

    Object projectsContext = request.getContext().get("projects");
    Object allocationPeriodsContext = request.getContext().get("allocationPeriods");

    if (projectsContext != null && allocationPeriodsContext != null) {
      ProjectRepository projectRepo = Beans.get(ProjectRepository.class);
      AllocationPeriodRepository allocationPeriodRepo = Beans.get(AllocationPeriodRepository.class);

      Set<Project> projects =
          ((List<LinkedHashMap<String, Object>>) projectsContext)
              .stream()
                  .map(project -> projectRepo.find(Long.valueOf(project.get("id").toString())))
                  .collect(Collectors.toSet());

      Set<AllocationPeriod> allocationPeriods =
          ((List<LinkedHashMap<String, Object>>) allocationPeriodsContext)
              .stream()
                  .map(
                      allocationPeriod ->
                          allocationPeriodRepo.find(
                              Long.valueOf(allocationPeriod.get("id").toString())))
                  .collect(Collectors.toSet());

      List<Sprint> sprintList =
          Beans.get(SprintService.class).generateSprints(projects, allocationPeriods);

      if (CollectionUtils.isNotEmpty(sprintList)) {
        response.setInfo(I18n.get("Sprints generated"));

        ActionView.ActionViewBuilder actionViewBuilder = ActionView.define(I18n.get("Sprints"));
        actionViewBuilder.model(Sprint.class.getName());
        actionViewBuilder.add("grid", "sprint-grid");
        actionViewBuilder.add("form", "sprint-form");
        actionViewBuilder.domain("self.id IN (:sprintIds)");
        actionViewBuilder.context(
            "sprintIds", sprintList.stream().map(Sprint::getId).collect(Collectors.toList()));

        response.setView(actionViewBuilder.map());
      }
    }
  }
}
