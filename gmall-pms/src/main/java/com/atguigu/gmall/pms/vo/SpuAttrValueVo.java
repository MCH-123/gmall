package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Data
public class SpuAttrValueVo extends SpuAttrValueEntity {
    public void setValueSelected(List<Object> valueSelected) {
        if (CollectionUtils.isEmpty(valueSelected)) {
            return;
        }
        this.setAttrValue(StringUtils.join(valueSelected,","));
    }
}
