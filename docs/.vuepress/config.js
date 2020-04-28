module.exports = {
  title: 'OkHttps',
  description: 'OkHttps 官网',
  head: [
    ['link', { rel: 'icon', href: '/logo.png' }]
  ],
  themeConfig: {
    logo: '/logo.png',
    nav: [
      { text: '教程', link: '/guide/' },
      { text: '历史版本', link: 'https://gitee.com/ejlchina-zhxu/okhttps/releases' },
      { text: 'Gitee', link: 'https://gitee.com/ejlchina-zhxu/okhttps' },
      { text: 'GitHub', link: 'https://github.com/ejlchina/okhttps' }
    ],
    sidebar: [
      ['/guide/', '起步'],
      ['/guide/foundation', '基础'],
      ['/guide/configuration', '配置'],
      ['/guide/features', '特色'],
      ['/guide/updown', '上传下载'],
      ['/guide/android', '安卓'],
    ],
    sidebarDepth: 2,
    smoothScroll: true,
    lastUpdated: '上次更新',
    repo: 'https://gitee.com/ejlchina-zhxu/okhttps',
    repoLabel: '查看源码',
    docsBranch: 'dev',
    editLinks: true,
  },
  markdown: {
    lineNumbers: true
  }
}