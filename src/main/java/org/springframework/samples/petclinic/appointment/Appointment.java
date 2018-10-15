/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.appointment;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.samples.petclinic.model.BaseEntity;
import org.springframework.samples.petclinic.vet.Vet;

/**
 * Simple JavaBean domain object representing an appointment.
 *
 * @author Ken Krebs
 * @author Dave Syer
 */
@Entity
@Table(name = "appointments")
public class Appointment extends BaseEntity implements Comparable<Appointment> {

    @Column(name = "appointment_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @Column(name = "timeslot")
    private String timeslot;

   
    @Column(name = "vet")
    private String vet;

    @NotEmpty
    @Column(name = "description")
    private String description;

    @Column(name = "pet_id")
    private Integer petId;

    /**
     * Creates a new instance of Visit for the current date
     */
    public Appointment() {
        this.date = findNextValidDay();
    }

    private LocalDate findNextValidDay() {
        LocalDate ret = LocalDate.now().plusDays(1);
        while (ret.getDayOfWeek() == DayOfWeek.SATURDAY || ret.getDayOfWeek() == DayOfWeek.SUNDAY) {
            ret = ret.plusDays(1);
        }
        return ret;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPetId() {
        return this.petId;
    }

    public void setPetId(Integer petId) {
        this.petId = petId;
    }

    public String getVet() {
        return vet;
    }

    public void setVet(String vet) {
        this.vet = vet;
    }

    public String getTimeslot() {
        return timeslot;
    }

    public void setTimeslot(String timeslot) {
        this.timeslot = timeslot;
    }

    @Override
    public int compareTo(Appointment o) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm a");
        LocalTime d1 = LocalTime.parse(timeslot, dtf);
        LocalTime d2 = LocalTime.parse(o.timeslot, dtf);
        return new CompareToBuilder().append(date, o.date).append(d1, d2).toComparison();
    }

}
