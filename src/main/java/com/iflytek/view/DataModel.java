package com.iflytek.view;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

/**
 * @author YZG 2017/12/24.
 */
@Data
public class DataModel implements Serializable{
    private String   sessionid;
    private String   filename;
    private Integer  statuscode;
    @JsonIgnore
    private Integer  status;

}
