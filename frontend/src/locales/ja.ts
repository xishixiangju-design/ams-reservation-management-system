export default {
  menu: {
    service: 'サービス管理',
    technician: '技術者管理',
    room: '部屋管理',
    appointment: '予約管理',
    customer: '会員管理',
    waitingList: 'キャンセル待ち',
    logs: '操作ログ',
    attendance: '勤怠管理',
    leaveRequest: '休暇申請管理',
    recordManagement: '打刻記録管理',
    myAppointments: '私の予約',
    myLeave: '休暇申請',
    myAttendance: '勤怠打刻'
  },
  common: {
    logout: 'ログアウト',
    login: 'ログイン',
    confirm: '確認',
    cancel: 'キャンセル',
    save: '保存',
    delete: '削除',
    edit: '編集',
    add: '追加',
    search: '検索',
    reset: 'リセット',
    operation: '操作',
    success: '成功',
    error: 'エラー',
    pleaseSelect: '選択してください',
    select: '予約',
    selected: '選択済み',
    pleaseInput: '入力してください',
    startTime: '開始時間',
    endTime: '終了時間',
    status: 'ステータス',
    createTime: '作成日時',
    remark: '備考',
    detail: '詳細',
    back: '戻る',
    submit: '送信',
    keyword: 'キーワード',
    timeRange: '時間範囲',
    to: '〜',
    actions: '操作',
    view: '表示',
    unknown: '不明',
    loading: '読み込み中...',
    noData: 'データなし'
  },
  login: {
    title: '予約管理システムログイン',
    account: 'アカウント',
    password: 'パスワード',
    captcha: '認証コード',
    rememberMe: 'ログイン状態を保存',
    forgotPassword: 'パスワードをお忘れですか？',
    loginBtn: 'ログイン',
    register: '新規登録',
    loggingIn: 'ログイン中...',
    resetPassword: 'パスワード再設定',
    verificationCode: '認証コード',
    newPassword: '新しいパスワード',
    confirmPassword: 'パスワード確認',
    sendCode: 'コード送信',
    resetBtn: 'パスワード再設定',
    backToLogin: 'ログインへ戻る',
    codeSent: '認証コードを送信しました',
    resetSuccess: 'パスワードが再設定されました',
    placeholder: {
      account: 'ユーザー名/携帯番号/メール',
      password: 'パスワードを入力',
      captcha: '認証コードを入力',
      email: 'メールアドレスを入力',
      verificationCode: '6桁のコードを入力',
      newPassword: '新しいパスワードを入力',
      confirmPassword: 'もう一度入力'
    },
    rules: {
      account: 'アカウントを入力してください',
      password: 'パスワードを入力してください',
      captcha: '認証コードを入力してください',
      emailRequired: 'メールアドレスを入力してください',
      emailInvalid: 'メールアドレスの形式が正しくありません',
      codeRequired: '認証コードを入力してください',
      passwordRequired: '新しいパスワードを入力してください',
      confirmPasswordRequired: '確認用パスワードを入力してください',
      passwordMismatch: 'パスワードが一致しません'
    },
    success: 'ログイン成功'
  },
  register: {
    title: '新規アカウント登録',
    username: 'ユーザー名/携帯番号/メール',
    nickname: 'ニックネーム',
    email: 'メールアドレス',
    password: 'パスワード',
    confirmPassword: 'パスワード（確認）',
    captcha: '認証コード',
    agree: '私は読み、同意しました',
    terms: '利用規約',
    privacy: 'プライバシーポリシー',
    submit: '登録',
    hasAccount: 'すでにアカウントをお持ちの方はこちら',
    placeholder: {
      username: 'ログインアカウントとして使用します',
      nickname: 'ニックネームを入力',
      email: '通知受信用メールアドレス',
      password: 'パスワードを入力',
      confirmPassword: 'パスワードを再入力',
      captcha: '認証コードを入力'
    },
    rules: {
      username: 'アカウントを入力してください',
      nickname: 'ニックネームを入力してください',
      email: '正しいメールアドレスを入力してください',
      password: 'パスワードは6文字以上で入力してください',
      confirmPassword: 'パスワードが一致しません',
      captcha: '認証コードを入力してください',
      agree: '利用規約に同意してください'
    },
    success: '登録成功、ログインしてください'
  },
  booking: {
    title: 'サービス予約',
    myAppointments: '私の予約',
    memberCenter: '会員センター',
    step1: 'サービス選択',
    step2: '技術者/時間選択',
    step3: '確認',
    logoutSuccess: 'ログアウトしました',
    anyTech: '指名なし',
    recommend: '推奨',
    selectDate: '予約日を選択',
    selectTime: '時間を選択',
    remaining: '残り{count}人',
    waitlist: 'キャンセル待ち',
    nextStep: '次へ',
    prevStep: '前へ',
    confirmTitle: '予約情報',
    serviceItem: 'サービス {index}',
    tech: '担当技術者',
    peopleCount: '予約人数',
    time: '予約時間',
    duration: '所要時間',
    originalPrice: '通常価格',
    discountPrice: '割引価格',
    contactInfo: '連絡先情報',
    name: '氏名',
    phone: '電話番号',
    total: '合計',
    submit: '予約する',
    successTitle: '予約完了！',
    subTitle: '以下のQRコードを提示して受付してください',
    qrCode: '確認コード',
    viewOrder: '注文詳細を見る',
    returnHome: 'ホームに戻る',
    waitlistConfirm:
      '選択した時間は満席です。キャンセル待ちリストに追加しますか？空きが出次第、すぐに通知されます。',
    joinWaitlist: 'キャンセル待ちに参加',
    waitlistSuccess: 'キャンセル待ちリストに追加されました！'
  },
  appointment: {
    list: {
      keywordPlaceholder: '顧客名/電話番号',
      status: {
        all: 'すべてのステータス',
        paid: '支払済み',
        completed: '完了',
        cancelled: 'キャンセル済み',
        violation: '無断キャンセル',
        pending: '来店待ち'
      },
      columns: {
        id: 'ID',
        customer: '顧客情報',
        time: '予約時間',
        service: 'サービス/人数',
        status: 'ステータス',
        remark: '備考',
        createTime: '作成日時'
      },
      actions: {
        detail: '詳細',
        cancel: 'キャンセル',
        cancelConfirm:
          'この予約をキャンセルしますか？（12時間以内のキャンセルは違反として記録されます）'
      }
    },
    create: {
      title: '新規予約（一括対応）',
      back: 'リストに戻る',
      steps: {
        customer: '顧客選択',
        service: 'サービス選択',
        time: '予約時間',
        confirm: '確認'
      },
      customer: {
        label: '顧客検索',
        placeholder: '電話番号またはニックネームで検索',
        notFound:
          '顧客が存在しない場合は、ユーザー管理で作成してください（または連絡先を直接入力してゲスト予約）',
        info: '選択された顧客情報',
        nickname: 'ニックネーム',
        phone: '電話番号',
        regTime: '登録日時',
        contactName: '連絡先氏名',
        contactPhone: '連絡先電話番号',
        guestName: 'ゲスト氏名',
        guestPhone: 'ゲスト電話番号'
      },
      service: {
        alert: '複数のサービスを選択して一括予約できます',
        name: 'サービス名',
        duration: '時間(分)',
        price: '価格(円)',
        desc: '説明',
        selected: '選択済み: {count} 件'
      },
      time: {
        date: '予約日',
        tech: '技術者指名',
        techPlaceholder: '指名なし（自動割り当て）',
        techNote: '注: 指名した技術者は、選択したすべてのサービスと時間帯に適用されます',
        slot: '時間帯選択',
        noDate: '日付を選択してください',
        noSlot: '利用可能な時間帯がありません',
        selected: '選択済み {count} 枠',
        summary:
          '({serviceCount} つのサービスに対して各 {timeCount} 件の予約を作成、計 {total} 件)',
        payment: '支払方法',
        offline: '現地払い',
        offlineNote: '管理者が作成した予約はデフォルトで現地払いです'
      },
      confirm: {
        title: '一括予約確認書',
        customer: '顧客',
        service: 'サービス項目',
        time: '予約時間',
        tech: '指名技術者',
        total: '予想合計件数'
      },
      submit: '送信確認',
      success: '{count} 件の予約を作成しました。今すぐ支払いますか？',
      payNow: '今すぐ支払う',
      payLater: '後で支払う',
      nextStep: '次へ',
      prevStep: '前へ'
    },
    detail: {
      title: '予約詳細',
      baseInfo: '基本情報',
      orderNo: '注文番号',
      orderTime: '注文日時',
      totalAmount: '合計金額',
      payStatus: '支払ステータス',
      payMethod: '支払方法',
      paid: '支払済み',
      unpaid: '未払い',
      customerName: '顧客名',
      customerPhone: '顧客電話',
      contact: '連絡先',
      contactPhone: '連絡先電話',
      serviceDetail: 'サービス詳細',
      serviceName: 'サービス名',
      unitPrice: '単価',
      tech: '指名技術者',
      room: '部屋割り当て',
      logs: '操作ログ',
      history: '顧客履歴',
      actions: '注文操作',
      markPaid: '支払済みにする（現地）',
      revokePay: '支払を取り消す',
      complete: '注文完了',
      reschedule: '変更/日程変更（開発中）',
      cancel: '予約キャンセル',
      notes: {
        title: '説明：',
        1: '1. 「来店待ち」ステータスのみキャンセル可能です。',
        2: '2. 「来店待ち」ステータスのみ完了操作が可能です。',
        3: '3. 開始時間まで12時間未満のキャンセルは違反として記録されます。'
      }
    }
  },
  customer: {
    toolbar: {
      placeholder: 'ユーザー名/ニックネーム検索',
      add: '会員追加'
    },
    columns: {
      id: 'ID',
      account: 'アカウント',
      nickname: 'ニックネーム',
      level: '会員ランク',
      discount: '割引率',
      violation: '違反回数',
      regTime: '登録日時'
    },
    level: {
      gold: 'ゴールド会員',
      platinum: 'プラチナ会員',
      normal: '一般会員'
    },
    dialog: {
      addTitle: '会員追加',
      editTitle: '会員情報編集',
      account: 'アカウント/携帯',
      password: 'パスワード',
      nickname: 'ニックネーム',
      email: 'メールアドレス',
      level: '会員ランク',
      discount: 'カスタム割引',
      discountTip: '会員ランクより優先されます（1.0 = 定価）',
      violation: '違反回数'
    }
  },
  dashboard: {
    todayAppt: '本日の予約',
    todayRevenue: '本日の売上',
    waitlist: 'キャンセル待ち',
    pending: 'サービス待ち',
    roomStatus: '部屋ステータス（リアルタイム）',
    techStatus: '技術者ステータス（ローテーション順）',
    calendar: '予約カレンダー',
    apptUnit: '件',
    refresh: '30秒自動更新',
    room: {
      cleaning: '清掃中',
      idle: '空き'
    },
    tech: {
      seq: '#',
      name: '技術者',
      status: '状態',
      detail: '詳細',
      busy: '施術中',
      leave: '休暇',
      idle: '待機中'
    }
  },
  logs: {
    module: 'モジュール',
    operator: '操作者',
    status: 'ステータス',
    success: '成功',
    fail: '失敗',
    columns: {
      id: 'ID',
      module: 'モジュール',
      type: '操作タイプ',
      operator: '操作者',
      ip: 'IPアドレス',
      time: '操作日時',
      status: 'ステータス',
      cost: '所要時間(ms)',
      error: 'エラー情報'
    }
  },
  technician: {
    add: '技術者追加',
    levels: {
      director: 'ディレクター',
      senior: 'シニア',
      junior: 'ジュニア'
    },
    columns: {
      id: 'ID',
      name: '氏名',
      account: 'アカウント/電話',
      status: 'ステータス'
    },
    status: {
      idle: '待機中',
      busy: '施術中',
      leave: '休暇'
    },
    dialog: {
      title: '技術者追加',
      name: '氏名',
      account: 'ログインID',
      password: 'パスワード',
      conflictTitle: '休暇申請不可：予約の競合',
      conflictMsg:
        'この技術者には以下の未完了の予約があります。先に処理してください（日程変更または担当者変更）：'
    }
  },
  attendance: {
    clockIn: '出勤打刻',
    clockOut: '退勤打刻',
    todayRecord: '本日の打刻記録',
    columns: {
      id: 'ID',
      type: 'タイプ',
      time: '打刻時間'
    },
    types: {
      in: '出勤',
      out: '退勤'
    },
    manager: {
      name: '技術者氏名',
      status: 'ステータス',
      columns: {
        id: '番号',
        tech: '技術者',
        type: 'タイプ',
        time: '時間',
        location: '場所',
        status: 'ステータス',
        createTime: '作成日時'
      },
      statusType: {
        normal: '正常',
        late: '遅刻',
        early: '早退',
        missing: '打刻忘れ'
      },
      markLate: '遅刻にする',
      markNormal: '正常にする'
    }
  },
  leave: {
    filter: {
      techName: '技術者名で検索',
      status: 'ステータス',
      pending: '承認待ち',
      approved: '承認済み',
      rejected: '却下'
    },
    button: {
      apply: '休暇申請',
      export: 'エクスポート',
      approve: '承認',
      reject: '却下'
    },
    table: {
      id: 'ID',
      techName: '技術者',
      type: 'タイプ',
      startTime: '開始時間',
      endTime: '終了時間',
      reason: '理由',
      status: 'ステータス',
      createTime: '作成日時'
    },
    type: {
      sick: '病欠',
      casual: '私用',
      annual: '有給休暇',
      other: 'その他'
    },
    dialog: {
      create: '休暇申請',
      cancel: 'キャンセル',
      confirm: '確認',
      success: '申請しました',
      approveConfirm: 'この申請を承認しますか？',
      rejectConfirm: 'この申請を却下しますか？',
      approveSuccess: '承認しました',
      rejectSuccess: '却下しました',
      range: '期間',
      reason: '理由'
    }
  },
  waitingList: {
    filter: {
      keyword: 'キーワード',
      status: 'ステータス',
      date: '希望日'
    },
    status: {
      waiting: '待機中',
      notified: '通知済み',
      converted: '予約確定',
      expired: '期限切れ',
      cancelled: 'キャンセル'
    },
    columns: {
      id: 'ID',
      customer: '顧客名',
      phone: '電話番号',
      service: '予約サービス',
      tech: '指名技術者',
      anyTech: '指名なし',
      date: '希望日',
      time: '希望時間帯',
      people: '人数',
      status: 'ステータス',
      joinTime: '参加日時'
    },
    convert: '予約へ変換',
    convertConfirm: 'このキャンセル待ちを正式な予約に変換しますか？'
  },
  room: {
    add: '部屋追加',
    columns: {
      id: 'ID',
      name: '部屋名',
      type: 'タイプ',
      capacity: '定員',
      status: 'ステータス'
    },
    types: {
      single: 'シングル',
      double: 'ダブル'
    },
    status: {
      idle: '空き',
      occupied: '使用中',
      maintenance: 'メンテナンス中'
    },
    dialog: {
      addTitle: '部屋追加',
      editTitle: '部屋編集',
      name: '部屋名',
      type: 'タイプ',
      status: 'ステータス'
    }
  },
  service: {
    add: 'サービス追加',
    columns: {
      id: 'ID',
      name: 'サービス名',
      desc: '説明',
      price: '価格',
      duration: '時間(分)',
      status: 'ステータス'
    },
    status: {
      on: '有効',
      off: '無効'
    },
    dialog: {
      addTitle: 'サービス追加',
      editTitle: 'サービス編集',
      name: 'サービス名',
      desc: '説明',
      price: '価格',
      duration: '時間(分)',
      status: 'ステータス'
    }
  },
  notification: {
    title: '通知リスト',
    markAll: 'すべて既読にする',
    empty: '通知はありません',
    delete: '削除'
  },
  myAppointments: {
    title: '私の予約',
    tabs: {
      official: '正式予約',
      waiting: 'キャンセル待ち'
    },
    status: {
      unpaid: '未払い',
      completed: '完了',
      cancelled: 'キャンセル',
      violation: '無断キャンセル',
      pending: '来店待ち'
    },
    waitingStatus: {
      waiting: '待機中',
      notified: '通知済み',
      converted: '予約確定',
      expired: '期限切れ'
    },
    refundStatus: {
      none: 'なし',
      processing: '返金処理中',
      completed: '返金済み',
      failed: '返金失敗'
    },
    labels: {
      refund: '返金ステータス：',
      service: 'サービス：',
      id: '予約番号：',
      people: '人数：',
      contact: '連絡先：',
      remark: '備考：',
      total: '合計：',
      expectedService: '希望サービス：',
      tech: '指名技術者：',
      time: '希望時間帯：',
      applyTime: '申請日時：',
      expireTime: '有効期限：'
    },
    actions: {
      pay: '支払う',
      reschedule: '日程変更',
      cancel: 'キャンセル',
      convertToBooking: '予約へ変換'
    },
    reschedule: {
      title: '予約日程変更',
      newTime: '新しい時間',
      changeTech: '技術者変更',
      keepTech: '同じ技術者を維持（任意）',
      confirm: '変更を確定'
    }
  },
  member: {
    level: '会員ランク',
    currentDiscount: '現在の割引',
    consumption: '現在の消費額',
    nextLevel: '{level}まで',
    need: 'あと',
    maxLevel: 'お客様は最高ランクの{level}です！',
    rights: '会員特典について',
    columns: {
      name: 'ランク名',
      threshold: '必要累計消費額',
      discount: '割引率',
      desc: '特典内容'
    }
  },
  payment: {
    success: '支払成功',
    successMsg: '支払が完了しました。ステータスは【来店待ち】です。',
    amount: '支払金額',
    time: '支払日時',
    fail: '支払失敗',
    failMsg: '支払処理中に問題が発生しました。再試行するか、サポートにお問い合わせください。',
    loading: '支払結果を確認中...',
    loadingMsg: 'お待ちください、決済データを同期しています',
    retry: '再試行',
    backHome: 'ホームに戻る',
    viewOrder: '予約を確認'
  }
}
