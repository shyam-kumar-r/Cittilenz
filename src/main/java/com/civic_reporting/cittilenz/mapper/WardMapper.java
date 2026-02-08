package com.civic_reporting.cittilenz.mapper;

import com.civic_reporting.cittilenz.dto.response.WardResponse;
import com.civic_reporting.cittilenz.entity.Ward;

public class WardMapper {

    public static WardResponse toResponse(Ward ward) {
        WardResponse r = new WardResponse();
        r.id = ward.getId();
        r.wardNumber = ward.getWardNumber();
        r.wardName = ward.getWardName();
        return r;
    }
}
