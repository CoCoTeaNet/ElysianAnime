<template>
  <el-row class="a-login">
    <div class="a-login-inner">
      <div class="a-login-left">
        <h2 style="text-align: center; color: #fff; ">
          ~欢迎登录追番JAnime~
        </h2>
      </div>
      <el-card class="a-login-right" shadow="always">
        <h2 style="text-align: center; padding-bottom: 16px;">
          欢 迎 登 录
        </h2>

        <el-form ref="loginFormRef" :model="loginForm" status-icon :rules="rules" size="large">
          <el-form-item prop="username">
            <el-input placeholder="账号" :prefix-icon="UserFilled" v-model="loginForm.username"
                      autocomplete="off">
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input placeholder="密码" :prefix-icon="Lock" v-model="loginForm.password"
                      @keypress.enter="submitForm(loginFormRef)"
                      type="password"
                      autocomplete="off">
            </el-input>
          </el-form-item>

          <el-form-item prop="captcha">
            <el-input style="width: 75%;" placeholder="验证码" :prefix-icon="Connection"
                      @keydown.enter="submitForm(loginFormRef)"
                      v-model="loginForm.captcha">
            </el-input>
            <el-image @click="getVerifyCodeImage" style="width: 25%;cursor: pointer" :src="captcha"/>
          </el-form-item>

          <el-form-item>
            <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
          </el-form-item>

          <el-form-item>
            <el-button style="width: 100%" type="primary" @click="submitForm(loginFormRef)" :loading="loading">
              登录
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </el-row>
</template>

<script setup lang="ts">
import {ref, reactive, onMounted, nextTick} from "vue";
import type {ElForm} from 'element-plus';
import {UserFilled, Lock, Connection} from "@element-plus/icons-vue";
import {getCaptcha, login} from "@/api/system/sys-login-api";
import {ElMessage} from "element-plus";
import {useRouter, useRoute} from "vue-router";
import 'element-plus/theme-chalk/display.css'
import {ApiResultEnum} from "@/api/ApiResultEnum";

const sm2 = require('sm-crypto').sm2;
const router = useRouter();
const route = useRoute();

type FormInstance = InstanceType<typeof ElForm>
const loginFormRef = ref<FormInstance>();

// 登录加载中
const loading = ref<boolean>(false);

// 验证码
const captcha = ref<string>('');

// 表单对象
const loginForm = reactive({
  username: '',
  password: '',
  captcha: '',
  captchaId: '', // 验证码ID
  publicKey: [], // 公钥
  rememberMe: true
});

// 表单校验规则
const rules = reactive({
  username: [{required: true, min: 2, max: 16, message: '长度限制2~16', trigger: 'blur'}],
  password: [{required: true, min: 6, max: 30, message: '长度限制6~30', trigger: 'blur'}],
  captcha: [{required: true, message: '请输入验证码', trigger: 'blur'}],
});

onMounted(() => {
  getVerifyCodeImage();
});

/**
 * 获取验证码
 */
const getVerifyCodeImage = () => {
  let timestamp = new Date().getTime();
  getCaptcha(timestamp).then((res: any) => {
    if (res.code === ApiResultEnum.SUCCESS) {
      captcha.value = `data:image/jpeg;base64,${res.data.imgBase64}`;
      loginForm.captchaId = res.data.captchaId;
      loginForm.publicKey = res.data.publicKey;
    }
  });
}

/**
 * 提交登录信息
 * @param formEl FormInstance
 */
const submitForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  formEl.validate((valid: any) => {
    if (valid) {
      loading.value = true;

      // see https://github.com/JuneAndGreen/sm-crypto/issues/72
      let encPassword = '04' + sm2.doEncrypt(loginForm.password, loginForm.publicKey);
      let loginParams = {password: ''};
      Object.assign(loginParams, loginForm);
      loginParams.password = encPassword;

      nextTick(() => {
        login(loginParams).then((res: any) => {
          if (res.code === ApiResultEnum.SUCCESS) {
            if (route.query.redirect) {
              if (route.query.redirect) {
                router.push({name: route.query.redirect + ''});
              }
            } else {
              router.push({name: 'AnimeHome'});
            }
          } else {
            ElMessage.error(!res.data ? res.message : res.data);
            getVerifyCodeImage();
          }
          loading.value = false;
        });
      });
    } else {
      console.log('error submit!');
      return false;
    }
  });
}
</script>

<style scoped src="./Login.css"></style>
