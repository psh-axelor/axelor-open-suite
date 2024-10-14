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
package com.axelor.apps.project.service.sprint;

import com.axelor.apps.project.db.AllocationPeriod;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.db.repo.SprintRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SprintServiceImpl implements SprintService {

  protected ProjectTaskRepository projectTaskRepo;
  protected SprintRepository sprintRepo;

  @Inject
  public SprintServiceImpl(ProjectTaskRepository projectTaskRepo, SprintRepository sprintRepo) {

    this.projectTaskRepo = projectTaskRepo;
    this.sprintRepo = sprintRepo;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void attachTasksToSprint(Sprint sprint, List<ProjectTask> projectTasks) {

    projectTasks.stream()
        .forEach(
            task -> {
              task.setSprint(sprint);
              projectTaskRepo.save(task);
            });
  }

  @Override
  public String allocationPeriodDomain(Set<Project> projects) {

    if (CollectionUtils.isEmpty(projects)) {
      return "self.id in (0)";
    }

    String companyIds =
        projects.stream()
            .filter(project -> project.getCompany() != null)
            .map(project -> project.getCompany().getId().toString())
            .distinct()
            .collect(Collectors.joining(","));

    return "self.company.id in (" + companyIds + ") and self.toDate >= '" + LocalDate.now() + "'";
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public List<Sprint> generateSprints(
      Set<Project> projects, Set<AllocationPeriod> allocationPeriods) {

    List<Sprint> sprintList = new ArrayList<>();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d");

    projects.forEach(
        project -> {
          allocationPeriods.forEach(
              allocationPeriod -> {
                Sprint sprint = new Sprint();
                sprint.setName(
                    I18n.get("Sprint")
                        + " "
                        + formatter.format(allocationPeriod.getFromDate())
                        + " - "
                        + formatter.format(allocationPeriod.getToDate()));
                sprint.setAllocationPeriod(allocationPeriod);
                sprint.setProject(project);
                sprintRepo.save(sprint);
                sprintList.add(sprint);
              });
        });

    return sprintList;
  }
}
