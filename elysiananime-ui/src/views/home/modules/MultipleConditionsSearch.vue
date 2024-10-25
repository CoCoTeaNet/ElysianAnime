<template>
  <el-collapse v-model="activeNames" style="width: 100%">

    <el-collapse-item title="追番进度" name="1">
      <el-checkbox v-model="readStatusCheckAll" :indeterminate="isIndeterminate" @change="statusListChange">
        全选
      </el-checkbox>
      <el-checkbox-group v-model="checkedStatusList" @change="statusChange">
        <el-checkbox v-for="(label, index) in readStatusList" :key="index" :label="label">
          {{ label }}
        </el-checkbox>
      </el-checkbox-group>
    </el-collapse-item>

    <el-collapse-item title="季度" name="2">
      <el-checkbox v-model="stateCheckAll" :indeterminate="isIndeterminateMonth" @change="monthListChange">
        全选
      </el-checkbox>
      <el-checkbox-group v-model="checkedMonthList" @change="monthChange">
        <el-checkbox v-for="(label, index) in timelineList.month" :key="index" :label="label">
          {{ label }}
        </el-checkbox>
      </el-checkbox-group>
    </el-collapse-item>

    <el-collapse-item title="标签" name="3">
      <el-checkbox v-model="yearCheckAll" :indeterminate="isIndeterminateState" @change="stateListChange">
        全选
      </el-checkbox>
      <el-checkbox-group v-model="checkedStateList" @change="stateChange">
        <el-checkbox v-for="(label, index) in timelineList.state" :key="index" :label="label">
          {{ label }}
        </el-checkbox>
      </el-checkbox-group>
    </el-collapse-item>

    <el-collapse-item title="年份" name="4">
      <el-checkbox v-model="monthCheckAll" :indeterminate="isIndeterminateYear" @change="yearListChange">
        全选
      </el-checkbox>
      <el-checkbox-group v-model="checkedYearList" @change="yearChange">
        <el-checkbox v-for="(label, index) in timelineList.year" :key="index" :label="label">
          {{ label }}
        </el-checkbox>
      </el-checkbox-group>
    </el-collapse-item>

    <el-collapse-item title="资源库" name="5">
      <el-form-item label="只显示有资源">
        <el-switch
            active-text="是"
            inactive-text="否"
            :active-value="1"
            :inactive-value="-1"
            inline-prompt
            v-model="hasResource" @change="hasResourceChange"
        />
      </el-form-item>
    </el-collapse-item>

  </el-collapse>
</template>

<script lang="ts" setup>
import {ref} from 'vue';

const emit = defineEmits(['onMultipleConditionsChange']);

// 选项卡默认第一个
const activeNames = ref(['1']);
// 追番进度组件属性
const readStatusCheckAll = ref(false);
const isIndeterminate = ref(true);
const checkedStatusList = ref(['']);
const readStatusList = ['未看', '已看', '正在观看'];
// 时间线筛选
const monthCheckAll = ref(false);
const stateCheckAll = ref(false);
const yearCheckAll = ref(false);
const checkedMonthList = ref(['']);
const checkedStateList = ref(['']);
const checkedYearList = ref(['']);
const isIndeterminateMonth = ref(true);
const isIndeterminateState = ref(true);
const isIndeterminateYear = ref(true);
const years: string[] = [];
const hasResource = ref(1);
for (let i = 0, c = new Date().getFullYear(); i < c - 2010; i++) {
  years.push('' + (c - i));
}
const timelineList = {
  month: ['1月', '4月', '7月', '10月'],
  state: ['已完结', '连载中', 'OVA', 'OAD', 'TV版', '剧场版'],
  year: [...years, '2010往前'],
}

//
// 全选状态变更监听
//
const statusListChange = (val: boolean) => {
  checkedStatusList.value = val ? readStatusList : [];
  isIndeterminate.value = false;
  emit('onMultipleConditionsChange', {'readStatus': checkedStatusList.value});
}

const monthListChange = (val: boolean) => {
  checkedMonthList.value = val ? timelineList.month : [];
  isIndeterminateMonth.value = false;
  emit('onMultipleConditionsChange', {'month': checkedMonthList.value});
}

const stateListChange = (val: boolean) => {
  checkedStateList.value = val ? timelineList.state : [];
  isIndeterminateState.value = false;
  emit('onMultipleConditionsChange', {'state': checkedStateList.value});
}

const yearListChange = (val: boolean) => {
  checkedYearList.value = val ? timelineList.year : [];
  isIndeterminateYear.value = false;
  emit('onMultipleConditionsChange', {'year': checkedYearList.value});
}

//
// 单选状态变更监听
//
const statusChange = (value: string[]) => {
  const checkedCount = value.length;
  readStatusCheckAll.value = checkedCount === readStatusList.length;
  isIndeterminate.value = checkedCount > 0 && checkedCount < readStatusList.length;
  emit('onMultipleConditionsChange', {'readStatus': value});
}

const monthChange = (value: string[]) => {
  const checkedCount = value.length;
  readStatusCheckAll.value = checkedCount === timelineList.month.length;
  isIndeterminateMonth.value = checkedCount > 0 && checkedCount < timelineList.month.length;
  emit('onMultipleConditionsChange', {'month': value});
}

const stateChange = (value: string[]) => {
  const checkedCount = value.length;
  readStatusCheckAll.value = checkedCount === timelineList.state.length;
  isIndeterminateState.value = checkedCount > 0 && checkedCount < timelineList.state.length;
  emit('onMultipleConditionsChange', {'state': value});
}

const yearChange = (value: string[]) => {
  const checkedCount = value.length;
  readStatusCheckAll.value = checkedCount === timelineList.year.length;
  isIndeterminateYear.value = checkedCount > 0 && checkedCount < timelineList.year.length;
  emit('onMultipleConditionsChange', {'year': value});
}

const hasResourceChange = (value: string[]) => {
  emit('onMultipleConditionsChange', {'hasResource': value});
}
</script>
