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

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.project.db.AllocationPeriod;
import com.axelor.apps.project.db.repo.AllocationPeriodRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AllocationPeriodServiceImpl implements AllocationPeriodService {

  protected AllocationPeriodRepository allocationPeriodRepo;
  protected WeeklyPlanningService weeklyPlanningService;

  @Inject
  public AllocationPeriodServiceImpl(
      AllocationPeriodRepository allocationPeriodRepo,
      WeeklyPlanningService weeklyPlanningService) {

    this.allocationPeriodRepo = allocationPeriodRepo;
    this.weeklyPlanningService = weeklyPlanningService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public List<AllocationPeriod> generateAllocationPeriods(
      Company company,
      LocalDate fromDate,
      LocalDate toDate,
      int nbOfDaysPerSprint,
      boolean considerWeekend) {

    List<AllocationPeriod> allocationPeriods = new ArrayList<>();
    LocalDate currentStartDate = fromDate;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d");

    if (!considerWeekend) {
      while (weeklyPlanningService.checkDateIsWeekend(company, currentStartDate)) {
        currentStartDate = currentStartDate.plusDays(1);
      }
    }

    while (currentStartDate.isBefore(toDate) || currentStartDate.isEqual(toDate)) {
      LocalDate endDate = currentStartDate;
      int daysCounted = 0;

      while (daysCounted < nbOfDaysPerSprint) {
        if (considerWeekend || !weeklyPlanningService.checkDateIsWeekend(company, endDate)) {
          daysCounted++;
        }
        endDate = endDate.plusDays(1);
      }

      if (endDate.isAfter(toDate.plusDays(1))) {
        endDate = toDate.plusDays(1);
        daysCounted = nbOfDaysPerSprint;
      }

      AllocationPeriod allocationPeriod = new AllocationPeriod();
      allocationPeriod.setFromDate(currentStartDate);
      allocationPeriod.setToDate(endDate.minusDays(1));
      allocationPeriod.setCompany(company);
      allocationPeriod.setName(
          I18n.get("Period")
              + " "
              + formatter.format(allocationPeriod.getFromDate())
              + " - "
              + formatter.format(allocationPeriod.getToDate()));

      allocationPeriodRepo.save(allocationPeriod);
      allocationPeriods.add(allocationPeriod);

      currentStartDate = endDate;

      if (!considerWeekend) {
        while (weeklyPlanningService.checkDateIsWeekend(company, currentStartDate)) {
          currentStartDate = currentStartDate.plusDays(1);
        }
      }
    }

    return allocationPeriods;
  }
}
