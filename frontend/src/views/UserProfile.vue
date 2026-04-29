<template>
  <div class="user-profile-container">
    <div class="content-layout">
      <!-- 左侧个人信息概览 -->
      <aside class="sidebar">
        <el-card class="profile-sidebar" shadow="hover">
          <div class="sidebar-header-bg"></div>
          <div class="user-info-overview">
            <el-upload
              class="avatar-uploader"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
            >
              <el-avatar :size="100" :src="profileForm.avatar" class="avatar-img">
                <el-icon class="avatar-uploader-icon"><Plus /></el-icon>
              </el-avatar>
              <div class="avatar-hover-mask">
                <el-icon><Camera /></el-icon>
                <span>更换头像</span>
              </div>
            </el-upload>
            <h2 class="user-nickname">
              {{ profileForm.nickname || '未设置昵称' }}
              <el-tag v-if="userLevel" :color="userLevel.icon" effect="dark" size="small" class="level-tag" style="border: none; margin-left: 8px;">
                {{ userLevel.levelName }}
              </el-tag>
            </h2>
            <p class="user-bio">{{ profileForm.bio || '这个人很懒，什么都没留下...' }}</p>
            
            <div class="user-tags">
              <el-tag v-if="profileForm.profession" size="small" effect="plain" type="info">{{ profileForm.profession }}</el-tag>
              <el-tag v-if="profileForm.location" size="small" effect="plain" type="info">{{ profileForm.location }}</el-tag>
            </div>

            <div class="follow-stats-row">
              <span class="follow-stat" @click="openFollowList('following')">
                <span class="follow-stat-count">{{ myFollowingCount }}</span>
                <span class="follow-stat-label">关注</span>
              </span>
              <span class="follow-stat-divider">|</span>
              <span class="follow-stat" @click="openFollowList('followers')">
                <span class="follow-stat-count">{{ myFollowerCount }}</span>
                <span class="follow-stat-label">粉丝</span>
              </span>
            </div>
          </div>

          <el-menu :default-active="activeTab" class="profile-menu" @select="handleSelectMenu" :border="false">
            <el-menu-item index="profile">
              <el-icon><User /></el-icon>
              <span>基础资料</span>
            </el-menu-item>
            <el-menu-item index="auth">
              <el-icon><Postcard /></el-icon>
              <span>实名认证</span>
            </el-menu-item>
            <el-menu-item index="account">
              <el-icon><Wallet /></el-icon>
              <span>我的账户</span>
            </el-menu-item>
            <el-menu-item index="password">
              <el-icon><Lock /></el-icon>
              <span>安全设置</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </aside>

      <!-- 右侧主要内容区 -->
      <main class="main-content-area">
        <el-card class="profile-content-card" shadow="hover">
          <div class="card-header-title">
            <span>{{ currentTabTitle }}</span>
          </div>

          <!-- 基础信息 -->
          <div v-show="activeTab === 'profile'" class="tab-content fade-in">
            <el-form :model="profileForm" label-width="100px" label-position="top" class="modern-form">
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="昵称">
                    <el-input v-model="profileForm.nickname" placeholder="请输入昵称" size="large" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="性别">
                    <el-select v-model="profileForm.gender" placeholder="请选择性别" size="large" style="width: 100%;">
                      <el-option label="保密" :value="0" />
                      <el-option label="男" :value="1" />
                      <el-option label="女" :value="2" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="生日">
                    <el-date-picker v-model="profileForm.birthday" type="date" placeholder="选择生日" format="YYYY-MM-DD" value-format="YYYY-MM-DD" size="large" style="width: 100%;" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="所在地">
                    <el-input v-model="profileForm.location" placeholder="例如：中国，北京" size="large" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="学历">
                    <el-select v-model="profileForm.education" placeholder="请选择学历" size="large" style="width: 100%;">
                      <el-option label="高中及以下" value="高中及以下" />
                      <el-option label="大专" value="大专" />
                      <el-option label="本科" value="本科" />
                      <el-option label="硕士" value="硕士" />
                      <el-option label="博士及以上" value="博士及以上" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="职业">
                    <el-input v-model="profileForm.profession" placeholder="请输入职业" size="large" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="24">
                <el-col :span="24">
                  <el-form-item label="公司 / 组织">
                    <el-input v-model="profileForm.company" placeholder="请输入公司或组织名称" size="large" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="24">
                <el-col :span="24">
                  <el-form-item label="个人简介">
                    <el-input v-model="profileForm.bio" type="textarea" :rows="4" placeholder="向大家介绍一下你自己吧..." resize="none" />
                  </el-form-item>
                </el-col>
              </el-row>

              <div class="form-actions">
                <el-button type="primary" size="large" @click="handleUpdateProfile" :loading="updating">保存更改</el-button>
              </div>
            </el-form>
          </div>

          <!-- 实名认证 -->
          <div v-show="activeTab === 'auth'" class="tab-content fade-in">
            <div class="auth-status-container" v-if="authInfo">
              <el-result v-if="authInfo.status === 0" icon="info" title="审核中" sub-title="您的实名认证信息正在审核中，请耐心等待。" />
              <el-result v-else-if="authInfo.status === 1" icon="success" title="认证成功" sub-title="您已完成实名认证，享受更多平台权益。" />
              <el-result v-else-if="authInfo.status === 2" icon="error" title="认证驳回" :sub-title="`驳回原因: ${authInfo.auditReason}`" />
            </div>

            <el-form :model="authForm" label-position="top" class="modern-form" :disabled="authInfo && (authInfo.status === 0 || authInfo.status === 1)">
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="真实姓名" required>
                    <el-input v-model="authForm.realName" placeholder="请输入真实姓名" size="large" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="身份证号" required>
                    <el-input v-model="authForm.idCard" placeholder="请输入18位身份证号" size="large" />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="身份证人像面" required>
                    <el-upload
                      class="idcard-uploader"
                      :action="uploadUrl"
                      :headers="uploadHeaders"
                      :show-file-list="false"
                      :on-success="handleIdCardFrontSuccess"
                      :disabled="authInfo && (authInfo.status === 0 || authInfo.status === 1)"
                    >
                      <img v-if="authForm.idCardFront" :src="authForm.idCardFront" class="idcard" />
                      <div v-else class="upload-placeholder">
                        <el-icon><Plus /></el-icon>
                        <span>点击上传人像面</span>
                      </div>
                    </el-upload>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="身份证国徽面" required>
                    <el-upload
                      class="idcard-uploader"
                      :action="uploadUrl"
                      :headers="uploadHeaders"
                      :show-file-list="false"
                      :on-success="handleIdCardBackSuccess"
                      :disabled="authInfo && (authInfo.status === 0 || authInfo.status === 1)"
                    >
                      <img v-if="authForm.idCardBack" :src="authForm.idCardBack" class="idcard" />
                      <div v-else class="upload-placeholder">
                        <el-icon><Plus /></el-icon>
                        <span>点击上传国徽面</span>
                      </div>
                    </el-upload>
                  </el-form-item>
                </el-col>
              </el-row>

              <div class="form-actions" v-if="!authInfo || authInfo.status === 2">
                <el-button type="primary" size="large" @click="submitAuth">提交认证</el-button>
              </div>
            </el-form>
          </div>

          <!-- 我的账户 -->
          <div v-show="activeTab === 'account'" class="tab-content fade-in">
            <div class="account-card balance-card-modern">
              <div class="balance-card__bg-pattern"></div>
              <div class="balance-card__content">
                <div class="balance-card__header">
                  <div class="balance-card__icon-wrap">
                    <el-icon class="balance-card__icon"><Wallet /></el-icon>
                  </div>
                  <div class="balance-card__label">可用余额</div>
                  <div class="balance-card__status">
                    <span class="balance-card__status-dot"></span>
                    <span>正常</span>
                  </div>
                </div>
                <div class="balance-card__amount">
                  <span class="balance-card__currency">¥</span>
                  <span class="balance-card__number">{{ balance.toFixed(2) }}</span>
                </div>
                <div class="balance-card__actions">
                  <el-button class="balance-card__btn balance-card__btn--primary" size="large" @click="showRechargeDialog = true">
                    <el-icon><Money /></el-icon> <span>充值</span>
                  </el-button>
                  <el-button v-if="isSponsorRole" class="balance-card__btn balance-card__btn--secondary" size="large" @click="showWithdrawalDialog = true">
                    <el-icon><Promotion /></el-icon> <span>提现</span>
                  </el-button>
                </div>
              </div>
            </div>

            <div class="record-switch-bar">
              <div class="record-switch-header">
                <div class="record-switch-title">资金明细</div>
                <div class="record-switch-tabs" role="tablist" aria-label="账户明细类型切换">
                  <button
                    type="button"
                    class="record-switch-tab"
                    :class="{ 'is-active': accountRecordView === 'transactions' }"
                    @click="switchAccountRecordView('transactions')"
                  >
                    全部流水
                  </button>
                  <button
                    type="button"
                    class="record-switch-tab"
                    :class="{ 'is-active': accountRecordView === 'recharge' }"
                    @click="switchAccountRecordView('recharge')"
                  >
                    充值明细
                  </button>
                  <button
                    v-if="isSponsorRole"
                    type="button"
                    class="record-switch-tab"
                    :class="{ 'is-active': accountRecordView === 'withdrawal' }"
                    @click="switchAccountRecordView('withdrawal')"
                  >
                    提现明细
                  </button>
                </div>
              </div>
              <p class="record-switch-hint">
                {{ accountRecordView === 'transactions' ? '展示每笔交易后的账户余额变动，让资金流向清晰可溯源。' : accountRecordView === 'recharge' ? '展示账户充值流水与支付状态。' : '展示提现申请进度与审核结果。' }}
              </p>
            </div>

            <div v-if="accountRecordView === 'transactions'" class="transactions-section">
              <div class="tx-filter-bar">
                <el-select v-model="transactionTypeFilter" placeholder="交易类型" clearable size="small" @change="fetchTransactions" style="width: 140px;">
                  <el-option label="全部" value="" />
                  <el-option label="项目支持" :value="1" />
                  <el-option label="平台退款" :value="2" />
                  <el-option label="项目拨付" :value="3" />
                  <el-option label="提现" :value="4" />
                  <el-option label="充值" :value="5" />
                </el-select>
              </div>
              <div v-loading="transactionLoading">
                <div v-if="transactions.length === 0 && !transactionLoading" class="tx-empty">
                  <el-empty description="暂无资金明细" :image-size="80" />
                </div>
                <div class="tx-table-wrap" v-else>
                  <table class="tx-table">
                    <thead>
                      <tr>
                        <th class="col-time">时间</th>
                        <th class="col-type">类型</th>
                        <th class="col-desc">描述</th>
                        <th class="col-amount">金额</th>
                        <th class="col-balance">余额</th>
                        <th class="col-status">状态</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="tx in transactions" :key="tx.id" class="tx-row">
                        <td class="col-time">
                          <span class="tx-date">{{ formatDate(tx.createdAt) }}</span>
                          <span class="tx-time">{{ formatTime(tx.createdAt) }}</span>
                        </td>
                        <td class="col-type">
                          <span class="tx-type-badge" :class="getTypeClass(tx.type)">
                            {{ getTransactionTypeText(tx.type) }}
                          </span>
                        </td>
                        <td class="col-desc">
                          <span class="tx-desc-main">{{ getTransactionDesc(tx) }}</span>
                          <span class="tx-desc-project" v-if="tx.projectName" @click="router.push(`/projects/${tx.projectId}`)">{{ tx.projectName }}</span>
                        </td>
                        <td class="col-amount">
                          <span class="tx-amount" :class="{ 'tx-amount-in': isTxIncome(tx.type), 'tx-amount-out': isTxOutcome(tx.type) }">
                            {{ isTxIncome(tx.type) ? '+' : '-' }}¥{{ formatNumber(tx.amount) }}
                          </span>
                        </td>
                        <td class="col-balance">
                          <span class="tx-balance">¥{{ formatNumber(tx.balanceAfter) }}</span>
                        </td>
                        <td class="col-status">
                          <el-tag size="small" :type="getTxStatusType(tx.status)" effect="plain">
                            {{ getTxStatusText(tx.status) }}
                          </el-tag>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
              <div class="pagination" v-if="transactionTotal > transactionSize">
                <el-pagination v-model:current-page="transactionPage" :page-size="transactionSize" layout="total, prev, pager, next" :total="transactionTotal" @current-change="fetchTransactions" small />
              </div>
            </div>

            <div v-else-if="accountRecordView === 'recharge'" class="recharge-section">
              <div v-loading="rechargeLoading">
                <table class="tx-table">
                  <thead>
                    <tr>
                      <th class="col-time">时间</th>
                      <th class="col-type">类型</th>
                      <th class="col-desc">描述</th>
                      <th class="col-amount">金额</th>
                      <th class="col-paytime">支付时间</th>
                      <th class="col-status">状态</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="record in rechargeRecords" :key="record.id" class="tx-row" :class="{ 'tx-row-pending': record.status === 0 }">
                      <td class="col-time">
                        <span class="tx-date">{{ formatDate(record.createdAt) }}</span>
                        <span class="tx-time">{{ formatTime(record.createdAt) }}</span>
                      </td>
                      <td class="col-type">
                        <span class="tx-type-badge type-recharge">
                          充值
                        </span>
                      </td>
                      <td class="col-desc">
                        <span class="tx-desc-main">账户充值</span>
                        <span class="tx-desc-project">{{ record.orderNo }}</span>
                      </td>
                      <td class="col-amount">
                        <span class="tx-amount tx-amount-in">+¥{{ formatNumber(record.amount) }}</span>
                      </td>
                      <td class="col-paytime">
                        <span v-if="record.payTime" class="tx-date">{{ formatDate(record.payTime) }}</span>
                        <span v-if="record.payTime" class="tx-time">{{ formatTime(record.payTime) }}</span>
                        <span v-else class="tx-time">-</span>
                      </td>
                      <td class="col-status">
                        <el-tag size="small" :type="record.status === 1 ? 'success' : 'warning'" effect="plain">
                          {{ record.status === 1 ? '已支付' : '待支付' }}
                        </el-tag>
                      </td>
                    </tr>
                    <tr v-if="rechargeRecords.length === 0 && !rechargeLoading" class="tx-empty-row">
                      <td colspan="6">
                        <el-empty description="暂无充值明细" :image-size="80" />
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div class="pagination" v-if="rechargeTotal > 10">
                <el-pagination v-model:current-page="rechargeCurrent" :page-size="10" layout="total, prev, pager, next" :total="rechargeTotal" @current-change="fetchRechargeRecords" small />
              </div>
            </div>

            <div v-else-if="isSponsorRole" class="withdrawal-section">
              <div v-loading="withdrawalLoading">
                <table class="tx-table">
                  <thead>
                    <tr>
                      <th class="col-time">时间</th>
                      <th class="col-type">方式</th>
                      <th class="col-desc">描述</th>
                      <th class="col-amount">金额</th>
                      <th class="col-paytime">驳回原因</th>
                      <th class="col-status">状态</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="record in withdrawalRecords" :key="record.id" class="tx-row" :class="{ 'tx-row-pending': record.status === 0 }">
                      <td class="col-time">
                        <span class="tx-date">{{ formatDate(record.createdAt) }}</span>
                        <span class="tx-time">{{ formatTime(record.createdAt) }}</span>
                      </td>
                      <td class="col-type">
                        <span class="tx-type-badge type-withdraw">
                          {{ record.type === 1 ? '支付宝' : '银行卡' }}
                        </span>
                      </td>
                      <td class="col-desc">
                        <span class="tx-desc-main">申请提现</span>
                        <span class="tx-desc-project">{{ record.orderNo }}</span>
                      </td>
                      <td class="col-amount">
                        <span class="tx-amount tx-amount-out">-¥{{ formatNumber(record.amount) }}</span>
                      </td>
                      <td class="col-paytime">
                        <span class="tx-time" :class="{ 'tx-desc-danger': record.status === 2 }">{{ record.rejectReason || '-' }}</span>
                      </td>
                      <td class="col-status">
                        <el-tag size="small" :type="record.status === 0 ? 'warning' : record.status === 1 ? 'success' : 'danger'" effect="plain">
                          {{ record.status === 0 ? '待审核' : record.status === 1 ? '已通过' : '已驳回' }}
                        </el-tag>
                      </td>
                    </tr>
                    <tr v-if="withdrawalRecords.length === 0 && !withdrawalLoading" class="tx-empty-row">
                      <td colspan="6">
                        <el-empty description="暂无提现明细" :image-size="80" />
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div class="pagination" v-if="withdrawalTotal > 10">
                <el-pagination v-model:current-page="withdrawalCurrent" :page-size="10" layout="total, prev, pager, next" :total="withdrawalTotal" @current-change="fetchWithdrawalRecords" small />
              </div>
            </div>
          </div>

          <!-- 修改密码 -->
          <div v-show="activeTab === 'password'" class="tab-content fade-in">
            <el-form :model="passwordForm" label-position="top" class="modern-form">
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="原密码" prop="oldPassword">
                    <el-input v-model="passwordForm.oldPassword" type="password" show-password size="large" placeholder="请输入当前密码" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="新密码" prop="newPassword">
                    <el-input v-model="passwordForm.newPassword" type="password" show-password size="large" placeholder="请输入新密码" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="确认新密码" prop="confirmPassword">
                    <el-input v-model="passwordForm.confirmPassword" type="password" show-password size="large" placeholder="请再次输入新密码" />
                  </el-form-item>
                </el-col>
              </el-row>
              <div class="form-actions">
                <el-button type="primary" size="large" @click="handleUpdatePassword">确认修改</el-button>
              </div>
            </el-form>
          </div>

        </el-card>
      </main>
    </div>

    <!-- 充值弹窗 -->
    <el-dialog v-model="showRechargeDialog" title="账户充值" width="400px" custom-class="modern-dialog">
      <div class="recharge-dialog-content">
        <div class="recharge-amount-input">
          <span class="label">充值金额：</span>
          <el-input-number 
            v-model="rechargeAmount" 
            :min="1" 
            :max="100000" 
            :precision="2" 
            :step="100"
            style="width: 200px"
          >
            <template #prefix>¥</template>
          </el-input-number>
        </div>
        <div class="payment-method">
          <span class="label">支付方式：</span>
          <el-radio-group v-model="rechargeMethod">
            <el-radio value="alipay">
              <div class="alipay-option">
                <el-icon><Platform /></el-icon> 支付宝 (Alipay)
              </div>
            </el-radio>
            <el-radio value="mock">
              <div class="mock-option">
                <el-icon><Money /></el-icon> 本地模拟充值 (Dev)
              </div>
            </el-radio>
          </el-radio-group>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showRechargeDialog = false">取消</el-button>
          <el-button type="primary" @click="handleRecharge" :loading="recharging">
            前往支付
          </el-button>
        </span>
      </template>
    </el-dialog>

    <FollowListDialog
      v-model="showFollowList"
      :user-id="userStore.userInfo?.id"
      :following-count="myFollowingCount"
      :follower-count="myFollowerCount"
      :initial-tab="followListTab"
      @counts-changed="onFollowCountsChanged"
    />

    <el-dialog v-model="showWithdrawalDialog" title="申请提现" width="480px" custom-class="modern-dialog">
      <el-form :model="withdrawalForm" label-width="90px" label-position="top" class="modern-form">
        <el-form-item label="提现金额">
          <el-input-number v-model="withdrawalForm.amount" :min="1" :max="balance" :precision="2" :step="100" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="提现方式">
          <el-radio-group v-model="withdrawalForm.type">
            <el-radio :value="1">支付宝</el-radio>
            <el-radio :value="2">银行卡</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="账户名">
          <el-input v-model="withdrawalForm.accountName" placeholder="请输入账户名" />
        </el-form-item>
        <el-form-item label="账号">
          <el-input v-model="withdrawalForm.accountNo" placeholder="请输入支付宝账号或银行卡号" />
        </el-form-item>
        <el-form-item label="银行名称" v-if="withdrawalForm.type === 2">
          <el-input v-model="withdrawalForm.bankName" placeholder="请输入银行名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showWithdrawalDialog = false">取消</el-button>
        <el-button type="primary" @click="handleWithdrawal" :loading="withdrawalSubmitting">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Camera, User, Postcard, Wallet, Lock, Platform, Money, Promotion } from '@element-plus/icons-vue'
import { updateProfile, updatePassword, getUserInfo, applyWithdrawal, getMyWithdrawals, getMyRechargeOrders } from '../api/user'
import { getFollowStatus } from '../api/follow'
import FollowListDialog from '../components/FollowListDialog.vue'
import request from '../utils/request'
import { useUserStore, isSponsor } from '../store/user'
import { useRouter, useRoute } from 'vue-router'

const uploadUrl = 'http://localhost:8080/api/file/upload'
const uploadHeaders = {
  Authorization: 'Bearer ' + localStorage.getItem('token')
}

const route = useRoute()
const STORAGE_KEY_TAB = 'user_profile_active_tab'
const activeTab = ref((route.query.tab as string) || (localStorage.getItem(STORAGE_KEY_TAB) as string) || 'profile')
const updating = ref(false)
const userStore = useUserStore()
const router = useRouter()
const userLevel = ref<any>(null)
const myFollowingCount = ref(0)
const myFollowerCount = ref(0)
const showFollowList = ref(false)
const followListTab = ref<'following' | 'followers'>('following')

const tabTitles: Record<string, string> = {
  profile: '基础资料',
  auth: '实名认证',
  account: '我的账户',
  password: '安全设置'
}

const currentTabTitle = computed(() => tabTitles[activeTab.value])

const handleSelectMenu = (index: string) => {
  activeTab.value = index
  localStorage.setItem(STORAGE_KEY_TAB, index)
}

const profileForm = ref({
  nickname: '',
  bio: '',
  avatar: '',
  gender: 0,
  birthday: '',
  location: '',
  education: '',
  profession: '',
  company: ''
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const balance = ref(0)
const showRechargeDialog = ref(false)
const rechargeAmount = ref(100)
const rechargeMethod = ref('mock')
const recharging = ref(false)
const isSponsorRole = computed(() => isSponsor(userStore.userInfo?.role))
const accountRecordView = ref<'recharge' | 'withdrawal' | 'transactions'>((localStorage.getItem('user_account_record_view') as any) || 'transactions')
const showWithdrawalDialog = ref(false)
const withdrawalSubmitting = ref(false)
const withdrawalRecords = ref<any[]>([])
const withdrawalLoading = ref(false)
const withdrawalCurrent = ref(1)
const withdrawalTotal = ref(0)
const withdrawalForm = ref({ amount: 100, type: 1, accountName: '', accountNo: '', bankName: '' })
const rechargeRecords = ref<any[]>([])
const rechargeLoading = ref(false)
const rechargeCurrent = ref(1)
const rechargeTotal = ref(0)

const transactions = ref<any[]>([])
const transactionLoading = ref(false)
const transactionPage = ref(1)
const transactionSize = ref(15)
const transactionTotal = ref(0)
const transactionTypeFilter = ref<number | string>('')

const fetchBalance = async () => {
  try {
    const res: any = await request.get('/user/account')
    balance.value = Number(res.data) || 0
  } catch (error) {
    console.error('Fetch balance error:', error)
  }
}

const handleRecharge = async () => {
  if (!rechargeAmount.value || rechargeAmount.value <= 0) {
    ElMessage.warning('请输入有效的充值金额')
    return
  }
  
  recharging.value = true
  try {
    if (rechargeMethod.value === 'mock') {
      const res: any = await request.post('/user/account/recharge/mock', {
        amount: rechargeAmount.value
      })
      if (res.code === 200) {
        ElMessage.success('模拟充值成功')
        showRechargeDialog.value = false
        fetchBalance()
        fetchRechargeRecords()
      } else {
        ElMessage.error(res.message || '模拟充值失败')
      }
    } else {
      const res: any = await request.post('/user/account/recharge', {
        amount: rechargeAmount.value
      })
      
      if (res.code === 200 && res.data) {
        const formHtml = res.data
        const div = document.createElement('div')
        div.innerHTML = formHtml
        document.body.appendChild(div)
        document.forms[document.forms.length - 1].submit()
      } else {
        ElMessage.error(res.message || '发起充值失败')
      }
    }
  } catch (error) {
    console.error('Recharge error:', error)
    ElMessage.error('发起充值失败')
  } finally {
    recharging.value = false
  }
}

const authInfo = ref<any>(null)
const authForm = ref({
  realName: '',
  idCard: '',
  idCardFront: '',
  idCardBack: ''
})

const loadUserInfo = async () => {
  try {
    const res: any = await getUserInfo()
    if (res.code === 200 && res.data) {
      const data = res.data
      profileForm.value = {
        nickname: data.nickname || '',
        bio: data.bio || '',
        avatar: data.avatar || '',
        gender: data.gender || 0,
        birthday: data.birthday || '',
        location: data.location || '',
        education: data.education || '',
        profession: data.profession || '',
        company: data.company || ''
      }
      userStore.userInfo = { ...userStore.userInfo, ...data }
    }
  } catch (error) {
    console.error('Failed to load user info', error)
  }
}

const handleAvatarSuccess = (res: any) => {
  if (res.code === 200) {
    profileForm.value.avatar = res.data
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}

const handleIdCardFrontSuccess = (res: any) => {
  if (res.code === 200) {
    authForm.value.idCardFront = res.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}

const handleIdCardBackSuccess = (res: any) => {
  if (res.code === 200) {
    authForm.value.idCardBack = res.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}

const fetchAuthInfo = async () => {
  try {
    const res: any = await request.get('/user/auth-info')
    authInfo.value = res.data
    if (res.data) {
      authForm.value.realName = res.data.realName || ''
      authForm.value.idCard = res.data.idCard || ''
      authForm.value.idCardFront = res.data.idCardFront || ''
      authForm.value.idCardBack = res.data.idCardBack || ''
    }
  } catch (error) {
    console.error(error)
  }
}

const submitAuth = async () => {
  if (!authForm.value.realName || !authForm.value.idCard || !authForm.value.idCardFront || !authForm.value.idCardBack) {
    ElMessage.warning('请填写完整的实名认证信息')
    return
  }
  try {
    await request.post('/user/auth-info', authForm.value)
    ElMessage.success('提交成功，等待审核')
    fetchAuthInfo()
  } catch (error: any) {
    ElMessage.error(error.message || '提交失败')
  }
}

const loadUserLevel = async () => {
  try {
    const res: any = await request.get('/user-level/current')
    if (res.code === 200 && res.data) {
      userLevel.value = res.data
    }
  } catch (error) {
    console.error('获取用户等级失败:', error)
  }
}

onMounted(() => {
  loadUserInfo()
  loadUserLevel()
  fetchAuthInfo()
  fetchBalance()
  if (accountRecordView.value === 'transactions') {
    fetchTransactions()
  } else if (accountRecordView.value === 'recharge') {
    fetchRechargeRecords()
  } else if (isSponsorRole.value) {
    fetchWithdrawalRecords()
  }
  loadFollowCounts()
  if (isSponsorRole.value) fetchWithdrawalRecords()
})

const loadFollowCounts = async () => {
  try {
    const userId = userStore.userInfo?.id
    if (!userId) return
    const res: any = await getFollowStatus(userId)
    if (res.code === 200 && res.data) {
      myFollowingCount.value = res.data.followingCount || 0
      myFollowerCount.value = res.data.followerCount || 0
    }
  } catch (e) {
    // ignore
  }
}

const openFollowList = (tab: 'following' | 'followers') => {
  followListTab.value = tab
  showFollowList.value = true
}

const onFollowCountsChanged = (data: { followingCount: number; followerCount: number }) => {
  myFollowingCount.value = data.followingCount
  myFollowerCount.value = data.followerCount
}

const fetchWithdrawalRecords = async () => {
  withdrawalLoading.value = true
  try {
    const res: any = await getMyWithdrawals({ current: withdrawalCurrent.value, size: 10 })
    withdrawalRecords.value = res.data.records || []
    withdrawalTotal.value = res.data.total || 0
  } catch (error) { console.error(error) }
  finally { withdrawalLoading.value = false }
}

const fetchRechargeRecords = async () => {
  rechargeLoading.value = true
  try {
    const res: any = await getMyRechargeOrders({ current: rechargeCurrent.value, size: 10 })
    rechargeRecords.value = res.data.records || []
    rechargeTotal.value = res.data.total || 0
  } catch (error) { console.error(error) }
  finally { rechargeLoading.value = false }
}

const switchAccountRecordView = (view: 'recharge' | 'withdrawal' | 'transactions') => {
  accountRecordView.value = view
  localStorage.setItem('user_account_record_view', view)

  if (view === 'transactions') {
    fetchTransactions()
  } else if (view === 'recharge') {
    fetchRechargeRecords()
  } else if (view === 'withdrawal' && isSponsorRole.value) {
    fetchWithdrawalRecords()
  }
}

const fetchTransactions = async () => {
  transactionLoading.value = true
  try {
    const params: any = { current: transactionPage.value, size: transactionSize.value }
    if (transactionTypeFilter.value !== '' && transactionTypeFilter.value !== null && transactionTypeFilter.value !== undefined) {
      params.type = transactionTypeFilter.value
    }
    const res: any = await request.get('/user/account/transactions', { params })
    transactions.value = res.data.records || []
    transactionTotal.value = res.data.total || 0
  } catch (error) {
    console.error('Fetch transactions error:', error)
  } finally {
    transactionLoading.value = false
  }
}

const formatNumber = (num: any) => {
  if (num === null || num === undefined) return '0.00'
  const n = Number(num)
  return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const formatDate = (dt: string) => {
  if (!dt) return ''
  const d = new Date(dt)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const formatTime = (dt: string) => {
  if (!dt) return ''
  const d = new Date(dt)
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:${String(d.getSeconds()).padStart(2, '0')}`
}

const getTransactionTypeText = (type: number) => {
  switch (type) {
    case 1: return '项目支持'
    case 2: return '平台退款'
    case 3: return '项目拨付'
    case 4: return '提现'
    case 5: return '充值'
    default: return '其他'
  }
}

const getTypeClass = (type: number) => {
  switch (type) {
    case 1: return 'type-support'
    case 2: return 'type-refund'
    case 3: return 'type-payout'
    case 4: return 'type-withdraw'
    case 5: return 'type-recharge'
    default: return 'type-other'
  }
}

const isTxIncome = (type: number) => type === 2 || type === 3 || type === 5
const isTxOutcome = (type: number) => type === 1 || type === 4

const getTxStatusType = (status: number) => {
  switch (status) {
    case 1: return 'success'
    case 0: return 'warning'
    case 2: return 'danger'
    default: return 'info'
  }
}

const getTxStatusText = (status: number) => {
  switch (status) {
    case 1: return '成功'
    case 0: return '处理中'
    case 2: return '失败'
    default: return '未知'
  }
}

const getTransactionDesc = (tx: any) => {
  switch (tx.type) {
    case 1: return '支持项目'
    case 2: return '退款到账'
    case 3: return '项目拨付'
    case 4: return '申请提现'
    case 5: return '账户充值'
    default: return '资金变动'
  }
}

const handleWithdrawal = async () => {
  if (!withdrawalForm.value.accountNo) { ElMessage.warning('请输入账号'); return }
  withdrawalSubmitting.value = true
  try {
    await applyWithdrawal(withdrawalForm.value)
    ElMessage.success('提现申请已提交')
    showWithdrawalDialog.value = false
    fetchBalance()
    fetchWithdrawalRecords()
  } catch (error: any) {
    ElMessage.error(error.message || '提现申请失败')
  } finally { withdrawalSubmitting.value = false }
}

const handleUpdateProfile = async () => {
  updating.value = true
  try {
    await updateProfile(profileForm.value)
    ElMessage.success('个人资料已保存')
    loadUserInfo() // reload to sync
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    updating.value = false
  }
}

const handleUpdatePassword = async () => {
  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword) {
    ElMessage.warning('请填写完整的密码信息')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  try {
    await updatePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
    router.push('/login')
  } catch (error: any) {
    ElMessage.error(error.message || '修改失败')
  }
}
</script>

<style scoped>
.user-profile-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: var(--spacing-4);
  animation: fadeIn var(--transition-fast);
}

.content-layout {
  display: flex;
  gap: var(--spacing-5);
}

.sidebar {
  width: 340px;
  flex-shrink: 0;
}

.main-content-area {
  flex: 1;
  min-width: 0;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.profile-sidebar {
  border-radius: var(--radius-xl);
  border: none;
  box-shadow: var(--shadow-md);
  margin-bottom: var(--spacing-3);
  position: relative;
  overflow: hidden;
  transition: box-shadow var(--transition-fast), transform var(--transition-fast);
}

.profile-sidebar:hover {
  box-shadow: var(--shadow-xl);
  transform: translateY(-2px);
}

.profile-sidebar :deep(.el-card__body) {
  padding: 0;
}

.sidebar-header-bg {
  height: 120px;
  background: var(--secondary-1);
  position: absolute;
  top: 0; left: 0; right: 0;
  z-index: 0;
}

.user-info-overview {
  position: relative;
  z-index: 1;
  padding: 60px var(--spacing-3) var(--spacing-3);
  text-align: center;
  border-bottom: 1px solid var(--border-light);
  background: var(--bg-surface);
}

.avatar-uploader {
  position: relative;
  display: inline-block;
  margin-bottom: var(--spacing-2);
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
}

.avatar-img {
  border: 4px solid var(--bg-surface);
  box-shadow: var(--shadow-lg);
  background: var(--bg-surface);
  transition: transform var(--transition-fast);
}

.avatar-hover-mask {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.5);
  color: var(--bg-surface);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity var(--transition-fast);
  font-size: var(--text-xs);
}

.avatar-uploader:hover .avatar-img {
  transform: scale(1.05);
}

.avatar-uploader:hover .avatar-hover-mask {
  opacity: 1;
}

.user-nickname {
  margin: 0 0 var(--spacing-1);
  font-size: var(--text-lg);
  font-weight: var(--font-weight-extrabold);
  color: var(--text-primary);
  font-family: var(--font-heading);
}

.follow-stats-row {
  margin-top: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-1);
}

.follow-stat {
  cursor: pointer;
  display: flex;
  align-items: baseline;
  gap: 4px;
  transition: color var(--transition-fast);
}

.follow-stat:hover {
  color: var(--color-primary);
}

.follow-stat-count {
  font-size: var(--text-base);
  font-weight: var(--font-weight-bold);
  color: var(--text-primary);
}

.follow-stat:hover .follow-stat-count {
  color: var(--color-primary);
}

.follow-stat-label {
  font-size: var(--text-xs);
  color: var(--text-secondary);
}

.follow-stat-divider {
  color: var(--border-color);
  font-size: var(--text-sm);
}

.user-bio {
  margin: 0 0 var(--spacing-2);
  font-size: var(--text-sm);
  color: var(--text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.user-tags {
  display: flex;
  justify-content: center;
  gap: var(--spacing-1);
  flex-wrap: wrap;
}

.profile-menu {
  padding: var(--spacing-2) 0;
}

.profile-menu .el-menu-item {
  height: 50px;
  line-height: 50px;
  margin: var(--spacing-1) var(--spacing-2);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  transition: all var(--transition-fast);
}

.profile-menu .el-menu-item:hover {
  background-color: var(--gray-100);
  transform: translateX(4px);
}

.profile-menu .el-menu-item.is-active {
  background: var(--color-primary-light);
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
  border-left: 4px solid var(--color-primary);
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
}

.profile-menu .el-icon {
  font-size: 18px;
  margin-right: var(--spacing-1);
}

.profile-content-card {
  border-radius: var(--radius-xl);
  border: none;
  box-shadow: var(--shadow-md);
  min-height: 600px;
  transition: box-shadow var(--transition-fast), transform var(--transition-fast);
}

.profile-content-card:hover {
  box-shadow: var(--shadow-xl);
  transform: translateY(-2px);
}

.profile-content-card :deep(.el-card__body) {
  padding: var(--spacing-5);
}

.card-header-title {
  font-size: var(--text-xl);
  font-weight: var(--font-weight-extrabold);
  color: var(--text-primary);
  margin-bottom: var(--spacing-5);
  position: relative;
  padding-bottom: var(--spacing-1);
  font-family: var(--font-heading);
}

.card-header-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 48px;
  height: 4px;
  background: var(--color-primary);
  border-radius: 2px;
}

.tab-content {
  width: 100%;
}

.fade-in {
  animation: fadeIn var(--transition-fast);
}

.modern-form :deep(.el-form-item__label) {
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  padding-bottom: var(--spacing-1);
}

.form-actions {
  margin-top: var(--spacing-4);
  padding-top: var(--spacing-3);
  border-top: 1px solid var(--border-light);
}

.auth-status-container {
  margin-bottom: var(--spacing-4);
  background: var(--gray-100);
  border-radius: var(--radius-md);
  padding: var(--spacing-2);
}

.idcard-uploader {
  width: 100%;
}

.idcard-uploader :deep(.el-upload) {
  width: 100%;
  border: 1px dashed var(--border-color);
  border-radius: var(--radius-md);
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all var(--transition-fast);
  background: var(--gray-100);
}

.idcard-uploader :deep(.el-upload:hover) {
  border-color: var(--color-primary);
}

.upload-placeholder {
  height: 240px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: var(--text-secondary);
}

.upload-placeholder .el-icon {
  font-size: 32px;
  margin-bottom: var(--spacing-1);
  color: var(--text-tertiary);
}

.idcard {
  width: 100%;
  height: 240px;
  object-fit: cover;
  display: block;
}

.balance-card-modern {
  position: relative;
  overflow: hidden;
  background: var(--bg-surface);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}

.balance-card-modern:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
}

.balance-card__bg-pattern {
  position: absolute;
  inset: 0;
  background-image: 
    linear-gradient(hsla(210, 15%, 70%, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, hsla(210, 15%, 70%, 0.04) 1px, transparent 1px);
  background-size: 24px 24px;
  z-index: 0;
  pointer-events: none;
}

.balance-card__content {
  position: relative;
  z-index: 1;
  padding: var(--spacing-5) var(--spacing-6);
}

.balance-card__header {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  margin-bottom: var(--spacing-3);
}

.balance-card__icon-wrap {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  background: hsl(210, 80%, 95%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.balance-card__icon {
  font-size: 20px;
  color: hsl(210, 80%, 55%);
}

.balance-card__label {
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--text-primary);
  flex: 1;
}

.balance-card__status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--text-xs);
  color: var(--color-success);
  background: hsl(140, 60%, 95%);
  padding: 4px 12px;
  border-radius: var(--radius-pill);
  font-weight: 600;
}

.balance-card__status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-success);
  animation: pulse-status 2s ease-in-out infinite;
}

@keyframes pulse-status {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.balance-card__amount {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: var(--spacing-4);
  padding: var(--spacing-4);
  background: hsl(220, 15%, 98%);
  border-radius: var(--radius-lg);
  border: 1px solid hsl(210, 15%, 92%);
}

.balance-card__currency {
  font-size: var(--text-xl);
  font-weight: 600;
  color: var(--text-secondary);
  margin-right: 2px;
}

.balance-card__number {
  font-size: 42px;
  font-weight: 800;
  color: var(--text-primary);
  letter-spacing: -0.02em;
  line-height: 1;
  font-family: var(--font-heading);
  font-feature-settings: 'tnum' 1;
}

.balance-card__actions {
  display: flex;
  gap: var(--spacing-3);
}

.balance-card__btn {
  flex: 1;
  height: 48px !important;
  border-radius: var(--radius-md) !important;
  font-size: var(--text-sm) !important;
  font-weight: 600 !important;
  transition: all var(--transition-fast) !important;
}

.balance-card__btn--primary {
  background: hsl(210, 80%, 55%) !important;
  border-color: hsl(210, 80%, 55%) !important;
  color: white !important;
}

.balance-card__btn--primary:hover {
  background: hsl(210, 80%, 48%) !important;
  border-color: hsl(210, 80%, 48%) !important;
  box-shadow: 0 4px 12px hsla(210, 80%, 55%, 0.3) !important;
}

.balance-card__btn--secondary {
  background: var(--bg-surface) !important;
  border: 1px solid var(--border-color) !important;
  color: var(--text-primary) !important;
}

.balance-card__btn--secondary:hover {
  background: var(--gray-50) !important;
  border-color: var(--color-primary) !important;
  color: var(--color-primary) !important;
}

.balance-card__btn .el-icon {
  margin-right: var(--spacing-1);
  font-size: 18px;
}

.modern-form :deep(.el-input__wrapper),
.modern-form :deep(.el-select__wrapper),
.modern-form :deep(.el-textarea__inner) {
  border-radius: var(--radius-md);
  box-shadow: 0 0 0 1px var(--border-light) inset;
  transition: all var(--transition-fast);
  background-color: var(--bg-surface);
}

.modern-form :deep(.el-input__wrapper:hover),
.modern-form :deep(.el-select__wrapper:hover),
.modern-form :deep(.el-textarea__inner:hover) {
  box-shadow: 0 0 0 1px var(--color-primary) inset;
}

.modern-form :deep(.el-input__wrapper.is-focus),
.modern-form :deep(.el-select__wrapper.is-focus),
.modern-form :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 2px var(--color-primary) inset;
  background-color: var(--color-primary-light);
}

.recharge-dialog-content {
  padding: 20px 0;
}

.recharge-amount-input, .payment-method {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.label {
  width: 80px;
  color: var(--text-secondary);
}

.alipay-option, .mock-option {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: var(--spacing-1) 15px;
  border: 1px solid var(--color-primary);
  color: var(--color-primary);
  border-radius: var(--radius-sm);
  background-color: var(--color-primary-light);
}

.mock-option {
  border-color: var(--color-success);
  color: var(--color-success);
  background-color: var(--color-accent-light);
}

.payment-method .el-radio {
  margin-right: var(--spacing-2);
  margin-bottom: var(--spacing-1);
}

.withdrawal-section {
  margin-top: var(--spacing-4);
}
.recharge-section {
  margin-top: var(--spacing-4);
}
.record-switch-bar {
  margin-top: var(--spacing-4);
  padding: var(--spacing-3);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  background: var(--bg-surface);
}
.record-switch-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-2);
}
.record-switch-title {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-bold);
  color: var(--text-primary);
}
.record-switch-tabs {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px;
  border-radius: var(--radius-md);
  background: var(--gray-100);
}
.record-switch-tab {
  border: none;
  background: transparent;
  color: var(--text-secondary);
  height: 36px;
  padding: 0 16px;
  border-radius: var(--radius-sm);
  font-size: var(--text-sm);
  font-weight: var(--font-weight-semibold);
  cursor: pointer;
  transition: background-color var(--transition-fast), color var(--transition-fast), box-shadow var(--transition-fast);
}
.record-switch-tab:hover {
  color: var(--text-primary);
}
.record-switch-tab.is-active {
  background: var(--bg-surface);
  color: var(--color-primary);
  box-shadow: var(--shadow-sm);
}
.record-switch-hint {
  margin: var(--spacing-2) 0 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
}
.section-divider {
  display: flex;
  align-items: center;
  margin-bottom: var(--spacing-3);
}
.section-divider::before,
.section-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--border-color);
}
.divider-text {
  padding: 0 var(--spacing-2);
  font-size: var(--text-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--text-secondary);
  white-space: nowrap;
}
.withdrawal-table {
  border-radius: var(--radius-md);
  overflow: hidden;
}
.recharge-table {
  border-radius: var(--radius-md);
  overflow: hidden;
}
.recharge-amount {
  font-weight: var(--font-weight-bold);
  color: var(--color-success);
}
.withdrawal-amount {
  font-weight: var(--font-weight-bold);
  color: var(--color-primary);
}

.transactions-section {
  margin-top: var(--spacing-4);
}

.tx-filter-bar {
  margin-bottom: var(--spacing-3);
}

.tx-empty {
  padding: var(--spacing-6) 0;
}

.tx-table-wrap {
  overflow-x: auto;
}

.tx-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  font-size: var(--text-sm);
}

.tx-table thead {
  position: sticky;
  top: 0;
  z-index: 2;
}

.tx-table th {
  background: var(--gray-50);
  color: var(--text-secondary);
  font-weight: 600;
  font-size: var(--text-xs);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding: var(--spacing-3);
  text-align: left;
  border-bottom: 2px solid var(--border-light);
  white-space: nowrap;
}

.tx-table th:first-child {
  border-radius: var(--radius-md) 0 0 0;
}

.tx-table th:last-child {
  border-radius: 0 var(--radius-md) 0 0;
}

.tx-row {
  transition: background var(--transition-fast);
}

.tx-row:hover {
  background: var(--gray-50);
}

.tx-row td {
  padding: var(--spacing-3);
  border-bottom: 1px solid var(--border-light);
  vertical-align: middle;
}

.col-time {
  width: 130px;
  white-space: nowrap;
}

.tx-date {
  display: block;
  font-weight: 500;
  color: var(--text-primary);
  font-size: var(--text-sm);
}

.tx-time {
  display: block;
  color: var(--text-tertiary);
  font-size: var(--text-xs);
  margin-top: 2px;
}

.col-type {
  width: 100px;
}

.tx-type-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: var(--radius-pill);
  font-size: var(--text-xs);
  font-weight: 600;
  white-space: nowrap;
}

.type-support {
  background: hsl(220, 100%, 95%);
  color: hsl(220, 80%, 45%);
}

.type-refund {
  background: hsl(160, 100%, 95%);
  color: hsl(160, 80%, 35%);
}

.type-payout {
  background: hsl(38, 100%, 95%);
  color: hsl(38, 90%, 40%);
}

.type-withdraw {
  background: hsl(280, 100%, 95%);
  color: hsl(280, 70%, 45%);
}

.type-recharge {
  background: hsl(145, 100%, 95%);
  color: hsl(145, 80%, 35%);
}

.type-other {
  background: var(--gray-100);
  color: var(--text-secondary);
}

.col-desc {
  min-width: 120px;
}

.tx-desc-main {
  display: block;
  color: var(--text-primary);
  font-weight: 500;
}

.tx-desc-project {
  display: block;
  color: var(--color-primary);
  font-size: var(--text-xs);
  cursor: pointer;
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 180px;
}

.tx-desc-project:hover {
  text-decoration: underline;
}

.col-amount {
  width: 110px;
  text-align: right;
  white-space: nowrap;
}

.tx-amount {
  font-family: var(--font-heading);
  font-weight: 800;
  font-size: var(--text-base);
  letter-spacing: -0.01em;
}

.tx-amount-in {
  color: var(--color-success);
}

.tx-amount-out {
  color: var(--color-danger);
}

.col-balance {
  width: 110px;
  text-align: right;
  white-space: nowrap;
}

.tx-balance {
  font-family: var(--font-heading);
  font-weight: 600;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  background: var(--gray-50);
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-light);
}

.col-status {
  width: 80px;
  text-align: center;
}

.tx-row-pending {
  background: hsl(45, 100%, 95%) !important;
}

.tx-row-pending:hover {
  background: hsl(45, 100%, 90%) !important;
}

.tx-row-pending .tx-amount {
  color: var(--color-warning);
}

.tx-empty-row td {
  padding: var(--spacing-6) var(--spacing-3);
  background: transparent !important;
  border-bottom: none;
}

.tx-desc-danger {
  color: var(--color-danger);
}

.transactions-section,
.recharge-section,
.withdrawal-section {
  min-height: 400px;
}

.tx-table {
  table-layout: fixed;
  width: 100%;
}

.tx-table .col-time { width: 130px; }
.tx-table .col-type { width: 90px; }
.tx-table .col-desc { min-width: 120px; }
.tx-table .col-amount { width: 110px; }
.tx-table .col-balance { width: 110px; }
.tx-table .col-status { width: 80px; }
.tx-table .col-paytime { width: 140px; }

@media (max-width: 1024px) {
  .content-layout {
    flex-direction: column;
  }
  .sidebar {
    width: 100%;
  }
  .modern-stat-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }
  .record-switch-header {
    flex-direction: column;
    align-items: flex-start;
  }
  .record-switch-tabs {
    width: 100%;
  }
  .record-switch-tab {
    flex: 1;
  }
  .stat-actions {
    width: 100%;
    display: flex;
    justify-content: flex-start;
  }
  .premium-btn {
    width: 100%;
    max-width: 220px;
  }
}

@media (max-width: 768px) {
  .user-profile-container {
    padding: var(--spacing-2);
  }
}
</style>
