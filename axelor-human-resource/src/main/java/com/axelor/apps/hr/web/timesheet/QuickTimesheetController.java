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
package com.axelor.apps.hr.web.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.EmploymentContract;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.service.timesheet.QuickTimesheetService;
import com.axelor.apps.hr.service.timesheet.TimesheetFetchService;
import com.axelor.apps.hr.service.timesheet.TimesheetProjectPlanningTimeService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.LinkedHashMap;

public class QuickTimesheetController {

  public void weekFromToDates(ActionRequest request, ActionResponse response) {

    Object dateContext = request.getContext().get("date");

    if (dateContext != null) {
      LocalDate date = LocalDate.parse(dateContext.toString());

      response.setValue("weekFromDate", date.with(DayOfWeek.MONDAY));
      response.setValue("weekToDate", date.with(DayOfWeek.SUNDAY));
    }
  }

  public void nextDate(ActionRequest request, ActionResponse response) {

    Object dateContext = request.getContext().get("date");
    Object employeeContext = request.getContext().get("employee");

    if (dateContext != null && employeeContext != null) {
      LocalDate date = LocalDate.parse(dateContext.toString());
      Long employeeId =
          Long.valueOf(((LinkedHashMap<String, Object>) employeeContext).get("id").toString());
      Employee employee = Beans.get(EmployeeRepository.class).find(employeeId);
      EmploymentContract employmentContract = employee.getMainEmploymentContract();
      Company company = (employmentContract != null) ? employmentContract.getPayCompany() : null;

      LocalDate nextDate = Beans.get(QuickTimesheetService.class).nextDate(employee, company, date);

      response.setValue("date", nextDate);
      response.setValue("weekFromDate", nextDate.with(DayOfWeek.MONDAY));
      response.setValue("weekToDate", nextDate.with(DayOfWeek.SUNDAY));
    }
  }

  public void previousDate(ActionRequest request, ActionResponse response) {

    Object dateContext = request.getContext().get("date");
    Object employeeContext = request.getContext().get("employee");

    if (dateContext != null && employeeContext != null) {
      LocalDate date = LocalDate.parse(dateContext.toString());
      Long employeeId =
          Long.valueOf(((LinkedHashMap<String, Object>) employeeContext).get("id").toString());
      Employee employee = Beans.get(EmployeeRepository.class).find(employeeId);
      EmploymentContract employmentContract = employee.getMainEmploymentContract();
      Company company = (employmentContract != null) ? employmentContract.getPayCompany() : null;

      LocalDate previousDate =
          Beans.get(QuickTimesheetService.class).previousDate(employee, company, date);

      response.setValue("date", previousDate);
      response.setValue("weekFromDate", previousDate.with(DayOfWeek.MONDAY));
      response.setValue("weekToDate", previousDate.with(DayOfWeek.SUNDAY));
    }
  }

  public void viewLines(ActionRequest request, ActionResponse response) throws AxelorException {

    Object dateContext = request.getContext().get("date");
    Object employeeContext = request.getContext().get("employee");

    if (dateContext != null && employeeContext != null) {
      LocalDate date = LocalDate.parse(dateContext.toString());
      Long employeeId =
          Long.valueOf(((LinkedHashMap<String, Object>) employeeContext).get("id").toString());
      Employee employee = Beans.get(EmployeeRepository.class).find(employeeId);

      Timesheet timesheet =
          Beans.get(TimesheetFetchService.class).getOrCreateOpenTimesheet(employee, date);

      QuickTimesheetService quickTimesheetService = Beans.get(QuickTimesheetService.class);

      if (!quickTimesheetService.checkLeaveAndPublicHoliday(employee, date)) {
        Beans.get(TimesheetProjectPlanningTimeService.class)
            .generateLinesFromExpectedProjectPlanning(timesheet);
        quickTimesheetService.generateLinesFromAllocationLines(employee, date, timesheet);
      }
    }

    ActionView.ActionViewBuilder actionViewBuilder = ActionView.define(I18n.get("Timesheet lines"));

    actionViewBuilder.model(TimesheetLine.class.getName());
    actionViewBuilder.add("grid", "quick-timesheet-line-grid");
    actionViewBuilder.add("form", "quick-timesheet-line-form");
    actionViewBuilder.param("popup", "true");
    actionViewBuilder.param("popup-save", "true");
    actionViewBuilder.param("forceEdit", "true");
    actionViewBuilder.domain("self.employee = :employee AND self.date = :date");

    response.setView(actionViewBuilder.map());
  }

  public void computeTotals(ActionRequest request, ActionResponse response) {

    Object dateContext = request.getContext().get("date");
    Object employeeContext = request.getContext().get("employee");

    if (dateContext != null && employeeContext != null) {
      LocalDate date = LocalDate.parse(dateContext.toString());
      Long employeeId =
          Long.valueOf(((LinkedHashMap<String, Object>) employeeContext).get("id").toString());
      Employee employee = Beans.get(EmployeeRepository.class).find(employeeId);

      QuickTimesheetService quickTimesheetService = Beans.get(QuickTimesheetService.class);

      response.setValue(
          "totalTimeEntryForDay",
          quickTimesheetService.computeTotalTimeEntryForDay(employee, date));
      response.setValue(
          "totalTimeEntryForWeek",
          quickTimesheetService.computeTotalTimeEntryForWeek(employee, date));
      response.setValue(
          "totalLeaveAndHolidayForDay",
          quickTimesheetService.totalLeaveAndHolidayForDay(employee, date));
      response.setValue(
          "totalLeaveAndHolidayForWeek",
          quickTimesheetService.totalLeaveAndHolidayForWeek(employee, date));
    }
  }

  public void leaveHolidayLabel(ActionRequest request, ActionResponse response) {

    Object dateContext = request.getContext().get("date");
    Object employeeContext = request.getContext().get("employee");

    if (dateContext != null && employeeContext != null) {
      LocalDate date = LocalDate.parse(dateContext.toString());
      Long employeeId =
          Long.valueOf(((LinkedHashMap<String, Object>) employeeContext).get("id").toString());
      Employee employee = Beans.get(EmployeeRepository.class).find(employeeId);

      response.setAttr(
          "leaveHolidayLabel",
          "hidden",
          !Beans.get(QuickTimesheetService.class).checkLeaveAndPublicHoliday(employee, date));
    }
  }

  public void newLine(ActionRequest request, ActionResponse response) throws AxelorException {

    Object dateContext = request.getContext().getParent().get("date");
    Object employeeContext = request.getContext().getParent().get("employee");

    if (dateContext != null && employeeContext != null) {
      LocalDate date = LocalDate.parse(dateContext.toString());
      Long employeeId =
          Long.valueOf(((LinkedHashMap<String, Object>) employeeContext).get("id").toString());
      Employee employee = Beans.get(EmployeeRepository.class).find(employeeId);

      Timesheet timesheet =
          Beans.get(TimesheetFetchService.class).getOrCreateOpenTimesheet(employee, date);

      ActionView.ActionViewBuilder actionViewBuilder =
          ActionView.define(I18n.get("Timesheet line"));

      actionViewBuilder.model(TimesheetLine.class.getName());
      actionViewBuilder.add("form", "quick-timesheet-line-form");
      actionViewBuilder.param("popup", "true");
      actionViewBuilder.param("popup-save", "true");
      actionViewBuilder.param("forceEdit", "true");
      actionViewBuilder.param("show-toolbar", "false");
      actionViewBuilder.context("_timesheet", timesheet);
      actionViewBuilder.context("_employee", employee);
      actionViewBuilder.context("_date", date);
      actionViewBuilder.context("_product", employee.getProduct());

      response.setView(actionViewBuilder.map());
    }
  }
}
