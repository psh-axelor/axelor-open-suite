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
package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.service.publicHoliday.PublicHolidayService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.service.leave.LeaveRequestService;
import com.axelor.apps.project.db.AllocationLine;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.AllocationLineRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class QuickTimesheetServiceImpl implements QuickTimesheetService {

  protected WeeklyPlanningService weeklyPlanningService;
  protected AllocationLineRepository allocationLineRepo;
  protected TimesheetLineRepository timesheetLineRepo;
  protected TimesheetLineCreateService timesheetLineCreateService;
  protected LeaveRequestService leaveRequestService;
  protected PublicHolidayService publicHolidayService;

  @Inject
  public QuickTimesheetServiceImpl(
      WeeklyPlanningService weeklyPlanningService,
      AllocationLineRepository allocationLineRepo,
      TimesheetLineRepository timesheetLineRepo,
      TimesheetLineCreateService timesheetLineCreateService,
      LeaveRequestService leaveRequestService,
      PublicHolidayService publicHolidayService) {

    this.weeklyPlanningService = weeklyPlanningService;
    this.allocationLineRepo = allocationLineRepo;
    this.timesheetLineRepo = timesheetLineRepo;
    this.timesheetLineCreateService = timesheetLineCreateService;
    this.leaveRequestService = leaveRequestService;
    this.publicHolidayService = publicHolidayService;
  }

  @Override
  public LocalDate nextDate(Employee employee, Company company, LocalDate date) {

    LocalDate nextDate = date.plusDays(1);

    WeeklyPlanning weeklyPlanning =
        (employee.getWeeklyPlanning() != null)
            ? employee.getWeeklyPlanning()
            : (company != null ? company.getWeeklyPlanning() : null);

    while (weeklyPlanningService.checkDateIsWeekend(weeklyPlanning, nextDate)) {
      nextDate = nextDate.plusDays(1);
    }

    return nextDate;
  }

  @Override
  public LocalDate previousDate(Employee employee, Company company, LocalDate date) {

    LocalDate previousDate = date.minusDays(1);

    WeeklyPlanning weeklyPlanning =
        (employee.getWeeklyPlanning() != null)
            ? employee.getWeeklyPlanning()
            : (company != null ? company.getWeeklyPlanning() : null);

    while (weeklyPlanningService.checkDateIsWeekend(weeklyPlanning, previousDate)) {
      previousDate = previousDate.minusDays(1);
    }

    return previousDate;
  }

  @Override
  public void generateLinesFromAllocationLines(
      Employee employee, LocalDate date, Timesheet timesheet) throws AxelorException {

    List<AllocationLine> allocationLineList =
        allocationLineRepo.findByUserAndDate(employee.getUser(), date).fetch();

    if (CollectionUtils.isNotEmpty(allocationLineList)) {

      for (AllocationLine allocationLine : allocationLineList) {
        List<ProjectTask> projectTaskList = allocationLine.getSprint().getProjectTaskList();

        if (CollectionUtils.isNotEmpty(projectTaskList)) {

          for (ProjectTask projectTask : projectTaskList) {

            if (timesheetLineRepo.findByEmployeeAndProjectTaskAndDate(employee, projectTask, date)
                == null) {
              timesheetLineCreateService.createTimesheetLine(
                  projectTask.getProject(),
                  projectTask,
                  employee.getProduct(),
                  date,
                  timesheet,
                  BigDecimal.ZERO,
                  "",
                  false);
            }
          }
        }
      }
    }
  }

  @Override
  public BigDecimal computeTotalTimeEntryForDay(Employee employee, LocalDate date) {

    return computeTimeEntryTotal(timesheetLineRepo.findByEmployeeAndDate(employee, date).fetch());
  }

  @Override
  public BigDecimal computeTotalTimeEntryForWeek(Employee employee, LocalDate date) {

    return computeTimeEntryTotal(
        timesheetLineRepo
            .findByEmployeeAndDates(
                employee, date.with(DayOfWeek.MONDAY), date.with(DayOfWeek.SUNDAY))
            .fetch());
  }

  protected BigDecimal computeTimeEntryTotal(List<TimesheetLine> timesheetLineList) {

    return CollectionUtils.isEmpty(timesheetLineList)
        ? BigDecimal.ZERO
        : timesheetLineList.stream()
            .map(TimesheetLine::getHoursDuration)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  @Override
  public boolean checkLeaveAndPublicHoliday(Employee employee, LocalDate date) {

    return leaveRequestService.isLeaveDay(employee, date)
        || publicHolidayService.checkPublicHolidayDay(
            date, employee.getPublicHolidayEventsPlanning());
  }

  @Override
  public BigDecimal totalLeaveAndHolidayForDay(Employee employee, LocalDate date) {

    return checkLeaveAndPublicHoliday(employee, date) ? BigDecimal.ONE : BigDecimal.ZERO;
  }

  @Override
  public BigDecimal totalLeaveAndHolidayForWeek(Employee employee, LocalDate date) {

    BigDecimal total = BigDecimal.ZERO;

    LocalDate fromDate = date.with(DayOfWeek.MONDAY);
    LocalDate toDate = date.with(DayOfWeek.SUNDAY);

    LocalDate currentDate = fromDate;

    while (!currentDate.isAfter(toDate)) {
      total = total.add(totalLeaveAndHolidayForDay(employee, currentDate));
      currentDate = currentDate.plusDays(1);
    }

    return total;
  }
}
