module.exports = {
  title: 'OkHttps',
  description: 'OkHttps 官网 比 Retrofit 更好用的网络框架！ OkHttp3 轻量封装 ，开箱即用、Java领域前后端处理 Http问题的新选择。',
  head: [
    ['link', { rel: 'icon', href: '/logo.png' }]
  ],
  themeConfig: {
    logo: '/logo.png',
    nav: [
      { 
        text: '教程',
        ariaLabel: '版本',
        items: [
          { text: 'v1.x', link: '/v1/' },
          { text: 'v2.x', link: '/v2/' }
        ]
      },
      { text: '历史版本', link: 'https://gitee.com/ejlchina-zhxu/okhttps/releases' },
      { text: 'Grails 中文', link: 'http://grails.ejlchina.com' },
      { text: '码云', link: 'https://gitee.com/ejlchina-zhxu/okhttps' }
    ],
    sidebar: {
      '/v1/': [
        '',
        'foundation',
        'configuration', 
        'features', 
        'updown',
        'android'
      ],
      '/v2/': [
        '',
        'foundation',
        'configuration', 
        'features', 
        'updown',
        'android'
      ]
    },
    sidebarDepth: 2,
    smoothScroll: true,
    lastUpdated: '上次更新',
    repo: 'ejlchina/okhttps',
    repoLabel: 'Github',
    docsBranch: 'dev',
    docsDir: 'docs',
    editLinks: true,
    editLinkText: '在 GitHub 上编辑此页',
    author: 'Troy Zhou'
  },
  // 若全局使用 vuepress，back-to-top 就会失效
  plugins: [
    '@vuepress/back-to-top', 'code-switcher', 'baidu-autopush', 'seo',
    ['baidu-tongji', {hm: '6eb41c0ab450d5a4ae8769682ecb0ab2'}],
  ],
  markdown: {
    lineNumbers: true
  }
}
