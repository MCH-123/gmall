package com.atguigu.gmall.pms.controller;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 属性分组
 *
 * @author menchuanhe
 * @email 1379325968@qq.com
 * @date 2023-01-27 17:01:24
 */
@Api(tags = "属性分组 管理")
@RestController
@RequestMapping("pms/attrgroup")
public class AttrGroupController {

    @Autowired
    private AttrGroupService attrGroupService;

    @ApiOperation("根据三级分类id查询")
    @GetMapping("category/{cid}")
    public ResponseVo<List<AttrGroupEntity>> queryByCidPage(@PathVariable Long cid) {
        List<AttrGroupEntity> groupEntityList = this.attrGroupService.list(new LambdaQueryWrapper<AttrGroupEntity>().eq(AttrGroupEntity::getCategoryId,cid));
        return ResponseVo.ok(groupEntityList);
    }

    @ApiOperation("根据三级分类id查询分组及组下的规格参数")
    @GetMapping("withattrs/{catId}")
    public ResponseVo<List<GroupVo>> queryByCid(@PathVariable Long catId) {
        List<GroupVo> groupVos = this.attrGroupService.queryByCid(catId);
        return ResponseVo.ok(groupVos);
    }

    @GetMapping("withattrvalues")
    public ResponseVo<List<ItemGroupVo>> queryGroupsBySpuIdAndCid(
            @RequestParam Long spuId,
            @RequestParam Long skuId,
            @RequestParam Long cid
    ) {
        List<ItemGroupVo> itemGroupVos = attrGroupService.queryGroupsBySpuIdAndCid(spuId, skuId, cid);
        return ResponseVo.ok(itemGroupVos);
    }
    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryAttrGroupByPage(PageParamVo paramVo) {
        PageResultVo pageResultVo = attrGroupService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<AttrGroupEntity> queryAttrGroupById(@PathVariable("id") Long id){
		AttrGroupEntity attrGroup = attrGroupService.getById(id);

        return ResponseVo.ok(attrGroup);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		attrGroupService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
