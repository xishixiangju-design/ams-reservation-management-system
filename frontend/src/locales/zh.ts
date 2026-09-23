export default {
  menu: {
    service: '服务管理',
    technician: '技师管理',
    room: '房间管理',
    appointment: '预约管理',
    customer: '会员管理',
    waitingList: '候补管理',
    logs: '操作日志',
    attendance: '出勤管理',
    leaveRequest: '请假申请管理',
    recordManagement: '打卡记录管理',
    myAppointments: '我的预约',
    myLeave: '请假申请',
    myAttendance: '考勤打卡'
  },
  common: {
    logout: '退出登录',
    login: '登录',
    confirm: '确认',
    cancel: '取消',
    save: '保存',
    delete: '删除',
    edit: '编辑',
    add: '新增',
    search: '搜索',
    reset: '重置',
    operation: '操作',
    success: '成功',
    error: '错误',
    pleaseSelect: '请选择',
    select: '预约',
    selected: '已选择',
    pleaseInput: '请输入',
    startTime: '开始时间',
    endTime: '结束时间',
    status: '状态',
    createTime: '创建时间',
    remark: '备注',
    detail: '详情',
    back: '返回',
    submit: '提交',
    keyword: '关键词',
    timeRange: '时间范围',
    to: '至',
    actions: '操作',
    view: '查看',
    unknown: '未知',
    loading: '加载中...',
    noData: '暂无数据',
    deleteConfirm: '确认删除'
  },
  login: {
    title: '预约管理系统登录',
    account: '账号',
    email: '邮箱',
    password: '密码',
    captcha: '验证码',
    rememberMe: '记住我',
    forgotPassword: '忘记密码？',
    loginBtn: '登录',
    register: '立即注册',
    loggingIn: '登录中...',
    resetPassword: '重置密码',
    verificationCode: '验证码',
    newPassword: '新密码',
    confirmPassword: '确认密码',
    sendCode: '发送验证码',
    resetBtn: '重置密码',
    backToLogin: '返回登录',
    codeSent: '验证码已发送',
    resetSuccess: '密码重置成功',
    placeholder: {
      account: '请输入用户名/手机号/邮箱',
      password: '请输入密码',
      captcha: '请输入验证码',
      email: '请输入邮箱',
      verificationCode: '请输入6位验证码',
      newPassword: '请输入新密码',
      confirmPassword: '请再次输入新密码'
    },
    rules: {
      account: '请输入账号',
      password: '请输入密码',
      captcha: '请输入验证码',
      emailRequired: '请输入邮箱',
      emailInvalid: '邮箱格式不正确',
      codeRequired: '请输入验证码',
      passwordRequired: '请输入新密码',
      confirmPasswordRequired: '请再次输入密码',
      passwordMismatch: '两次输入密码不一致'
    },
    success: '登录成功'
  },
  register: {
    title: '注册新账号',
    username: '用户名/手机/邮箱',
    nickname: '昵称',
    email: '邮箱',
    password: '密码',
    confirmPassword: '确认密码',
    captcha: '验证码',
    agree: '我已阅读并同意',
    terms: '服务条款',
    privacy: '隐私政策',
    submit: '注册',
    hasAccount: '已有账号，立即登录',
    placeholder: {
      username: '请输入作为登录账号',
      nickname: '请输入昵称',
      email: '请输入邮箱（用于接收通知）',
      password: '请输入密码',
      confirmPassword: '请再次输入密码',
      captcha: '请输入验证码'
    },
    rules: {
      username: '请输入账号',
      nickname: '请输入昵称',
      email: '请输入正确的邮箱地址',
      password: '密码长度不能少于6位',
      confirmPassword: '两次输入密码不一致',
      captcha: '请输入验证码',
      agree: '请勾选服务条款'
    },
    success: '注册成功，请登录'
  },
  booking: {
    title: '预约服务',
    myAppointments: '我的预约',
    memberCenter: '会员中心',
    step1: '选择服务',
    step2: '选择技师/时间',
    step3: '确认信息',
    logoutSuccess: '退出成功',
    anyTech: '任意技师',
    recommend: '推荐',
    selectDate: '选择预约日期',
    selectTime: '选择时间',
    remaining: '剩{count}人',
    waitlist: '候补',
    nextStep: '下一步',
    prevStep: '上一步',
    confirmTitle: '预约信息',
    serviceItem: '服务项目 {index}',
    tech: '服务技师',
    peopleCount: '预约人数',
    time: '预约时间',
    duration: '总时长',
    originalPrice: '原价',
    discountPrice: '优惠价',
    contactInfo: '联系信息',
    name: '姓名',
    phone: '手机号',
    total: '合计',
    submit: '立即预约',
    successTitle: '预约成功!',
    subTitle: '请出示下方二维码核销',
    qrCode: '核销码',
    viewOrder: '查看订单详情',
    returnHome: '返回首页',
    waitlistConfirm: '当前时段资源已满，是否加入候补队列？一旦有空位将立即通知您。',
    joinWaitlist: '加入候补',
    waitlistSuccess: '已成功加入候补队列！'
  },
  appointment: {
    list: {
      keywordPlaceholder: '客户姓名/电话',
      status: {
        all: '全部状态',
        paid: '已支付', // Status 2 is COMPLETED/PAID in some context, but backend maps 2 to Completed
        completed: '已完成',
        cancelled: '已取消',
        violation: '违约',
        pending: '待消费'
      },
      columns: {
        id: 'ID',
        customer: '客户信息',
        time: '预约时间',
        service: '服务/人数',
        status: '状态',
        remark: '备注',
        createTime: '创建时间'
      },
      actions: {
        detail: '详情',
        cancel: '取消',
        cancelConfirm: '确定取消该预约吗？(不足12小时将记录违约)'
      }
    },
    create: {
      title: '新建预约 (支持批量)',
      back: '返回列表',
      steps: {
        customer: '选择客户',
        service: '选择服务',
        time: '预约时间',
        confirm: '确认信息'
      },
      customer: {
        label: '查找客户',
        placeholder: '输入手机号或昵称搜索',
        notFound: '如果客户不存在，请先在用户管理中创建 (或直接输入联系方式作为散客预约)',
        info: '已选客户信息',
        nickname: '昵称',
        phone: '手机号',
        regTime: '注册时间',
        contactName: '联系人姓名',
        contactPhone: '联系电话',
        guestName: '散客姓名',
        guestPhone: '散客电话'
      },
      service: {
        alert: '您可以选择多个服务项目进行批量预约',
        name: '服务名称',
        duration: '时长(分钟)',
        price: '价格(元)',
        desc: '描述',
        selected: '已选服务: {count} 项'
      },
      time: {
        date: '预约日期',
        tech: '指定技师',
        techPlaceholder: '不指定（自动分配）',
        techNote: '注: 指定技师将应用于所有选中的服务和时段',
        slot: '选择时段',
        noDate: '请先选择日期',
        noSlot: '该日期无可用时段',
        selected: '已选 {count} 个时段',
        summary: '(将为 {serviceCount} 个服务各创建 {timeCount} 个预约，共 {total} 单)',
        payment: '支付方式',
        offline: '线下付款',
        offlineNote: '管理员新建预约默认线下支付'
      },
      confirm: {
        title: '批量预约确认单',
        customer: '客户',
        service: '服务项目',
        time: '预约时间',
        tech: '指定技师',
        total: '预计总单数'
      },
      submit: '确认提交',
      success: '成功创建 {count} 个预约。是否立即支付？',
      payNow: '立即支付',
      payLater: '暂不支付',
      nextStep: '下一步',
      prevStep: '上一步'
    },
    detail: {
      title: '预约详情',
      baseInfo: '基本信息',
      orderNo: '订单号',
      orderTime: '下单时间',
      totalAmount: '总金额',
      payStatus: '支付状态',
      payMethod: '支付方式',
      paid: '已支付',
      unpaid: '未支付',
      customerName: '客户昵称',
      customerPhone: '客户手机',
      contact: '联系人',
      contactPhone: '联系电话',
      serviceDetail: '服务明细',
      serviceName: '服务名称',
      unitPrice: '单价',
      tech: '指定技师',
      room: '分配房间',
      logs: '操作日志',
      history: '客户历史预约',
      actions: '订单操作',
      markPaid: '标记已付款 (线下)',
      revokePay: '撤销付款',
      complete: '完成订单',
      reschedule: '变更/改期 (待开发)',
      cancel: '取消预约',
      notes: {
        title: '说明：',
        1: '1. 只有“待消费”状态可操作取消。',
        2: '2. 只有“待消费”状态可操作完成订单。',
        3: '3. 距离开始时间不足12小时取消将记录违约。'
      }
    }
  },
  customer: {
    toolbar: {
      placeholder: '搜索用户名/昵称',
      add: '新增会员'
    },
    columns: {
      id: 'ID',
      account: '账号',
      nickname: '昵称',
      level: '会员等级',
      discount: '折扣率',
      violation: '违约次数',
      regTime: '注册时间'
    },
    level: {
      gold: '黄金会员',
      platinum: '铂金会员',
      normal: '普通会员'
    },
    dialog: {
      addTitle: '新增会员',
      editTitle: '编辑会员信息',
      account: '账号/手机',
      password: '密码',
      nickname: '昵称',
      email: '邮箱',
      level: '会员等级',
      discount: '自定义折扣',
      discountTip: '优先级高于会员等级 (1.0 = 原价)',
      violation: '违约次数'
    }
  },
  dashboard: {
    todayAppt: '今日预约',
    todayRevenue: '今日营收',
    waitlist: '候补人数',
    pending: '待服务',
    roomStatus: '房间状态 (实时)',
    techStatus: '技师状态 (轮牌顺位)',
    calendar: '预约日历',
    apptUnit: '单',
    refresh: '30s 自动刷新',
    room: {
      cleaning: '打扫中',
      idle: '空闲'
    },
    tech: {
      seq: '#',
      name: '技师',
      status: '状态',
      detail: '详情',
      busy: '服务中',
      leave: '休假',
      idle: '空闲'
    }
  },
  logs: {
    module: '模块',
    operator: '操作人',
    status: '状态',
    success: '成功',
    fail: '失败',
    columns: {
      id: 'ID',
      module: '模块',
      type: '操作类型',
      operator: '操作人',
      ip: 'IP地址',
      time: '操作时间',
      status: '状态',
      cost: '耗时(ms)',
      error: '错误信息'
    }
  },
  technician: {
    add: '新增技师',
    levels: {
      director: '总监',
      senior: '资深',
      junior: '初级'
    },
    columns: {
      id: 'ID',
      name: '姓名',
      account: '账号/电话',
      status: '状态',
      level: '职级',
      activeOrders: '进行中订单',
      totalOrders: '历史总订单'
    },
    status: {
      idle: '空闲',
      busy: '忙碌',
      leave: '请假'
    },
    dialog: {
      title: '新增技师',
      name: '姓名',
      account: '登录账号',
      password: '登录密码',
      passwordPlaceholderEdit: '留空则不修改',
      introCn: '中文简介',
      introJp: '日文简介',
      conflictTitle: '无法请假：存在预约冲突',
      conflictMsg: '该技师在未来有以下未完成的预约，请先处理（改期或更换技师）：'
    }
  },
  attendance: {
    clockIn: '上班打卡',
    clockOut: '下班打卡',
    todayRecord: '今日考勤记录',
    columns: {
      id: 'ID',
      type: '类型',
      time: '打卡时间'
    },
    types: {
      in: '上班',
      out: '下班'
    },
    manager: {
      name: '技师姓名',
      status: '状态',
      columns: {
        id: '编号',
        tech: '技师',
        type: '类型',
        time: '时间',
        location: '地点',
        status: '状态',
        createTime: '创建时间'
      },
      statusType: {
        normal: '正常',
        late: '迟到',
        early: '早退',
        missing: '缺卡'
      },
      markLate: '标记迟到',
      markNormal: '标记正常'
    }
  },
  leave: {
    filter: {
      techName: '按技师姓名搜索',
      status: '状态',
      pending: '待审批',
      approved: '已批准',
      rejected: '已拒绝'
    },
    button: {
      apply: '申请请假',
      export: '导出',
      approve: '批准',
      reject: '拒绝'
    },
    table: {
      id: '编号',
      techName: '技师',
      type: '类型',
      startTime: '开始时间',
      endTime: '结束时间',
      reason: '原因',
      status: '状态',
      createTime: '创建时间'
    },
    type: {
      sick: '病假',
      casual: '事假',
      annual: '年假',
      other: '其他'
    },
    dialog: {
      create: '申请请假',
      cancel: '取消',
      confirm: '确定',
      success: '申请已提交',
      approveConfirm: '确定要批准此申请吗？',
      rejectConfirm: '确定要拒绝此申请吗？',
      approveSuccess: '批准成功',
      rejectSuccess: '拒绝成功',
      range: '时间范围',
      reason: '原因'
    }
  },
  waitingList: {
    filter: {
      keyword: '关键词',
      status: '状态',
      date: '期望日期'
    },
    status: {
      waiting: '等待中',
      notified: '已通知',
      converted: '已转正',
      expired: '已过期',
      cancelled: '已取消'
    },
    columns: {
      id: 'ID',
      customer: '客户姓名',
      phone: '手机号',
      service: '预约服务',
      tech: '指定技师',
      anyTech: '任意技师',
      date: '期望日期',
      time: '期望时段',
      people: '人数',
      status: '状态',
      joinTime: '加入时间'
    },
    convert: '转正',
    convertConfirm: '确定将此候补转为正式预约吗？'
  },
  room: {
    add: '新增房间',
    columns: {
      id: 'ID',
      name: '房间名称',
      type: '类型',
      capacity: '容量',
      status: '状态'
    },
    types: {
      single: '单人间',
      double: '双人间'
    },
    status: {
      idle: '空闲',
      occupied: '使用中',
      maintenance: '维护中'
    },
    dialog: {
      addTitle: '新增房间',
      editTitle: '编辑房间',
      name: '房间名称',
      type: '类型',
      status: '状态'
    }
  },
  service: {
    add: '新增服务',
    columns: {
      id: 'ID',
      name: '服务名称',
      desc: '描述',
      price: '价格',
      duration: '时长(分钟)',
      status: '状态'
    },
    status: {
      on: '上架',
      off: '下架'
    },
    dialog: {
      addTitle: '新增服务',
      editTitle: '编辑服务',
      name: '服务名称',
      desc: '描述',
      price: '价格',
      duration: '时长(分钟)',
      status: '状态'
    }
  },
  notification: {
    title: '通知列表',
    markAll: '全部已读',
    empty: '暂无通知',
    delete: '删除'
  },
  myAppointments: {
    title: '我的预约',
    tabs: {
      official: '正式预约',
      waiting: '候补记录'
    },
    status: {
      unpaid: '待支付',
      completed: '已完成',
      cancelled: '已取消',
      violation: '违约取消',
      pending: '待消费'
    },
    waitingStatus: {
      waiting: '候补中',
      notified: '已通知',
      converted: '已转正',
      expired: '已过期'
    },
    refundStatus: {
      none: '无',
      processing: '退款中',
      completed: '已退款',
      failed: '退款失败'
    },
    labels: {
      refund: '退款状态：',
      service: '服务项目：',
      id: '预约编号：',
      people: '人数：',
      contact: '联系人：',
      remark: '备注：',
      total: '总价：',
      expectedService: '期望服务：',
      tech: '指定技师：',
      time: '期望时段：',
      applyTime: '申请时间：',
      expireTime: '过期时间：'
    },
    actions: {
      pay: '立即支付',
      reschedule: '改期',
      cancel: '取消预约',
      convertToBooking: '转为预约'
    },
    reschedule: {
      title: '预约改期',
      newTime: '新时间',
      changeTech: '更换技师',
      keepTech: '保持原技师 (可选)',
      confirm: '确定改期'
    }
  },
  member: {
    level: '会员等级',
    currentDiscount: '当前折扣',
    consumption: '当前消费',
    nextLevel: '距离 {level}',
    need: '还需',
    maxLevel: '尊贵的{level}，您已达到最高等级！',
    rights: '会员权益说明',
    columns: {
      name: '等级名称',
      threshold: '累计消费门槛',
      discount: '享受折扣',
      desc: '专属权益'
    }
  },
  payment: {
    success: '支付成功',
    successMsg: '您的订单已支付，状态为【待消费】。',
    amount: '支付金额',
    time: '支付时间',
    fail: '支付失败',
    failMsg: '支付过程中出现问题，请重试或联系客服。',
    loading: '正在确认支付结果...',
    loadingMsg: '请稍候，我们正在同步支付宝数据',
    retry: '重新支付',
    backHome: '返回首页',
    viewOrder: '查看我的预约'
  }
}
