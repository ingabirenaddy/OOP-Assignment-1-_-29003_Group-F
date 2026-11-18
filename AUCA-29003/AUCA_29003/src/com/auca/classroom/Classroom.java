package com.auca.classroom;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;

/** A physical classroom. Can be registered without any other record. */
public class Classroom implements Identifiable, Registrable {

    private final String classroomId;
    private String buildingName;
    private String roomNumber;
    private int capacity;
    private int floorNumber;
    private boolean available;

    public Classroom(String classroomId, String buildingName, String roomNumber,
                     int capacity, int floorNumber, boolean available) {
        this.classroomId = classroomId;
        this.buildingName = buildingName;
        this.roomNumber = roomNumber;
        this.capacity = capacity;
        this.floorNumber = floorNumber;
        this.available = available;
    }

    @Override
    public String getId() {
        return classroomId;
    }

    @Override
    public String getDisplayName() {
        return buildingName + " room " + roomNumber;
    }

    @Override
    public boolean isActive() {
        return available;
    }

    @Override
    public void validate() throws ValidationException {
        Identifiable.validateId(classroomId, "Classroom ID");
        Registrable.validateName(buildingName, "Building name");
        Registrable.validateCode(roomNumber, "Room number", 1, 10);
        Registrable.requireRange(capacity, 1, 1000, "Capacity");
        Registrable.requireRange(floorNumber, 0, 200, "Floor number");
    }

    @Override
    public void printDetails() {
        Registrable.printHeading("CLASSROOM " + classroomId + "   (" + Identifiable.referenceOf(this) + ")");
        Registrable.printRow("Classroom ID", classroomId);
        Registrable.printRow("Building name", buildingName);
        Registrable.printRow("Room number", roomNumber);
        Registrable.printRow("Capacity", capacity);
        Registrable.printRow("Floor number", floorNumber);
        Registrable.printRow("Available", available ? "YES" : "NO");
    }

    @Override
    public String getRegistrationSummary() {
        return String.format("Classroom     [%s] %-26s seats %d, floor %d | %s", classroomId, getDisplayName(),
                capacity, floorNumber, available ? "AVAILABLE" : "NOT AVAILABLE");
    }

    @Override
    public String toString() {
        return "Classroom[" + classroomId + ", " + getDisplayName() + ", capacity=" + capacity
                + ", floor=" + floorNumber + ", available=" + available + "]";
    }

    // ------------------------------------------------------------------ getters / setters

    public String getClassroomId() {
        return classroomId;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
