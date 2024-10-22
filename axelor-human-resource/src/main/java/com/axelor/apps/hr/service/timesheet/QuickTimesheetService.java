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
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface QuickTimesheetService {

  public LocalDate nextDate(Employee employee, Company company, LocalDate date);

  public LocalDate previousDate(Employee employee, Company company, LocalDate date);

  public void generateLinesFromAllocationLines(
      Employee employee, LocalDate date, Timesheet timesheet) throws AxelorException;

  public BigDecimal computeTotalTimeEntryForDay(Employee employee, LocalDate date);

  public BigDecimal computeTotalTimeEntryForWeek(Employee employee, LocalDate date);

  public boolean checkLeaveAndPublicHoliday(Employee employee, LocalDate date);

  public BigDecimal totalLeaveAndHolidayForDay(Employee employee, LocalDate date);

  public BigDecimal totalLeaveAndHolidayForWeek(Employee employee, LocalDate date);
}
