package com.rehome.main.dto.schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenDataDTO {
    @JsonProperty("animal_subid")
    private String caseNumber;

    @JsonProperty("animal_kind")
    private String species;

    @JsonProperty("animal_Variety")
    private String breed;

    @JsonProperty("animal_sex")
    private String gender;

    @JsonProperty("animal_bodytype")
    private String size;

    @JsonProperty("animal_colour")
    private String color;
    
    @JsonProperty("animal_age")
    private String age;

    @JsonProperty("animal_sterilization")
    private String sterilization;

    @JsonProperty("animal_foundplace")
    private String foundPlace;

    @JsonProperty("animal_status")
    private String status;

    @JsonProperty("animal_remark")
    private String remark;

    @JsonProperty("shelter_name")
    private String shelterName;

    @JsonProperty("animal_opendate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate opendate;

    @JsonProperty("animal_createtime")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createAt;

    @JsonProperty("album_file")
    private String imageUrl;
}
