module.exports = {
  title: 'OkHttps',
  locales: {
    '/': {
      lang: 'zh-CN',
      description: 'OkHttps 官网 比 Retrofit 更好用的网络框架！ OkHttp3 轻量封装 ，开箱即用、Java领域前后端处理 Http问题的新选择。',
    },
    '/en/': {
      lang: 'en-US',
      description: 'A very lightweight and powerful HTTP client for Java and Android.',
    }
  },
  head: [
    ['link', { rel: 'icon', href: '/logo.png' }]
  ],
  themeConfig: {
    logo: '/logo.png',
    locales: {
      '/': {
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
            'getstart',
            'foundation',
            'configuration',  
            'updownload',
            'websocket',
            'stomp',
            'android',
            'questions'
          ]
        },
        lastUpdated: '上次更新',
        editLinkText: '在 GitHub 上编辑此页'
      },
      '/en/': {
        nav: [
          { text: 'Guide', link: '/en/v2/' },
          { text: 'History Verions', link: 'https://github.com/ejlchina/okhttps/releases' },
          { text: 'Grails Chinese', link: 'http://grails.ejlchina.com' },
          { text: 'Gitee', link: 'https://gitee.com/ejlchina-zhxu/okhttps' }
        ],
        sidebar: {
          '/en/v2/': [
            '',
            'getstart',
            'foundation',
            'configuration',  
            'updownload',
            'android'
          ]
        },
        lastUpdated: 'Last Updated',
        editLinkText: 'Edit this page on GitHub',
      }
    },
    sidebarDepth: 2,
    smoothScroll: true,
    repo: 'ejlchina/okhttps',
    repoLabel: 'Github',
    docsBranch: 'dev',
    docsDir: 'docs',
    editLinks: true,
    author: 'Troy Zhou'
  },
  // 若全局使用 vuepress，back-to-top 就会失效
  plugins: [
    '@vuepress/back-to-top', 
    '@vuepress/medium-zoom', 
    'baidu-autopush', 'seo',
    ['baidu-tongji', {hm: '6eb41c0ab450d5a4ae8769682ecb0ab2'}],
    ['@vssue/vuepress-plugin-vssue', {
      // 设置 `platform` 而不是 `api`
      platform: 'github-v4',
      // 其他的 Vssue 配置
      owner: 'ejlchina',
      repo: 'okhttps',
      clientId: '4e308eb78d3a64abd2a8',
      clientSecret: '17bf99c60a396b66e8ac841e3dc7a6256ab6cbcf',
      autoCreateIssue: true
    }]
  ],
  markdown: {
    lineNumbers: true
  }
}
