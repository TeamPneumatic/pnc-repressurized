/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.event;

import java.util.Calendar;

public class DateEventHandler {
    public static boolean isEvent() {
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.MONTH) + 1 == 4 && calendar.get(Calendar.DATE) == 17) {//MineMaarten's birthday
            return true;
        } else if (calendar.get(Calendar.MONTH) + 1 == 12 && calendar.get(Calendar.DATE) == 31) {//New Years eve
            return true;
        } else //MineMaarten released his first mod
            if (calendar.get(Calendar.MONTH) + 1 == 6 && calendar.get(Calendar.DATE) == 9) {//PneumaticCraft's birthday
            return true;
        } else return calendar.get(Calendar.MONTH) + 1 == 2 && calendar.get(Calendar.DATE) == 19;
    }
}
