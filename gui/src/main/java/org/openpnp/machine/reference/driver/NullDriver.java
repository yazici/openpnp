/*
 	Copyright (C) 2011 Jason von Nieda <jason@vonnieda.org>
 	
 	This file is part of OpenPnP.
 	
	OpenPnP is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    OpenPnP is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with OpenPnP.  If not, see <http://www.gnu.org/licenses/>.
 	
 	For more information about OpenPnP visit http://openpnp.org
 */

package org.openpnp.machine.reference.driver;

import java.util.HashMap;

import org.openpnp.machine.reference.ReferenceActuator;
import org.openpnp.machine.reference.ReferenceDriver;
import org.openpnp.machine.reference.ReferenceHead;
import org.openpnp.machine.reference.ReferenceHeadMountable;
import org.openpnp.machine.reference.ReferenceNozzle;
import org.openpnp.model.LengthUnit;
import org.openpnp.model.Location;
import org.openpnp.spi.Head;
import org.simpleframework.xml.Attribute;

/**
 * An example of the simplest possible driver that can support multiple heads.
 * This driver maintains a set of coordinates for each Head that it is asked to
 * handle and simply logs all commands sent to it.
 */
public class NullDriver implements ReferenceDriver {
    @Attribute(required = false)
    private String dummy;
    
    private HashMap<Head, Location> headLocations = new HashMap<Head, Location>();

    /**
     * Gets the Location object being tracked for a specific Head. This is the
     * absolute coordinates of a virtual Head on the machine.
     * 
     * @param head
     * @return
     */
    protected Location getHeadLocation(Head head) {
        Location l = headLocations.get(head);
        if (l == null) {
            l = new Location(LengthUnit.Millimeters, 0, 0, 0, 0);
            headLocations.put(head, l);
        }
        return l;
    }

    @Override
    public void home(ReferenceHead head) throws Exception {
        Location l = getHeadLocation(head);
        l.setX(0);
        l.setY(0);
        l.setZ(0);
        l.setRotation(0);
    }

    /**
     * Return the Location of a specific ReferenceHeadMountable on the machine.
     * We get the coordinates for the Head the object is attached to, and then
     * we add the offsets assigned to the object to make the coordinates correct
     * for that object.
     */
    @Override
    public Location getLocation(ReferenceHeadMountable hm) {
        return getHeadLocation(hm.getHead()).add(hm.getHeadOffsets());
    }

    /**
     * Commands the driver to move the given ReferenceHeadMountable to the
     * specified Location at the given speed. Please see the comments for this
     * method in the code for some important considerations when writing your
     * own driver.
     */
    @Override
    public void moveTo(ReferenceHeadMountable hm, Location location,
            double speed) throws Exception {
        // Subtract the offsets from the incoming Location. This converts the
        // offset coordinates to driver / absolute coordinates.
        location = location.subtract(hm.getHeadOffsets());

        // Convert the Location to millimeters, since that's the unit that
        // this driver works in natively.
        location = location.convertToUnits(LengthUnit.Millimeters);

        // Get the current location of the Head that we'll move
        Location hl = getHeadLocation(hm.getHead());

        // Now that movement is complete, update the stored Location to the new
        // Location, unless the incoming Location specified an axis with a value
        // of NaN. NaN is interpreted to mean "Don't move this axis" so we don't
        // update the value, either.
        if (!Double.isNaN(location.getX())) {
            hl.setX(location.getX());
        }
        if (!Double.isNaN(location.getY())) {
            hl.setY(location.getY());
        }
        if (!Double.isNaN(location.getZ())) {
            hl.setZ(location.getZ());
        }
        if (!Double.isNaN(location.getRotation())) {
            hl.setRotation(location.getRotation());
        }
    }

    @Override
    public void pick(ReferenceNozzle nozzle) throws Exception {
    }

    @Override
    public void place(ReferenceNozzle nozzle) throws Exception {
    }

    @Override
    public void actuate(ReferenceActuator actuator, double value)
            throws Exception {
    }

    @Override
    public void actuate(ReferenceActuator actuator, boolean on)
            throws Exception {
    }

    @Override
    public void setEnabled(boolean enabled) throws Exception {
    }
}
