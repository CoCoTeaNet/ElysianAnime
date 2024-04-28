const {defineConfig} = require('@vue/cli-service');
const CompressionWebpackPlugin = require('compression-webpack-plugin');
const AutoImport = require('unplugin-auto-import/webpack')
const Components = require('unplugin-vue-components/webpack')
const {ElementPlusResolver} = require('unplugin-vue-components/resolvers')

console.log('打包场景：' + (process.env.ENV === 'development' ? '开发环境' : '生产环境'));

module.exports = defineConfig({
    transpileDependencies: true, productionSourceMap: process.env.ENV === 'development', configureWebpack: config => {
        config.plugins.push(new CompressionWebpackPlugin({
            filename: '[path][name].gz', test: /\.(js|css)$/i
        }));
        config.plugins.push(AutoImport({
            resolvers: [ElementPlusResolver()],
        }));
        config.plugins.push(Components({
            resolvers: [ElementPlusResolver()],
        }));
    }, devServer: {
        port: 8086,
        proxy: {
            '/api': {
                target: 'http://127.0.0.1:8088/v2-api', changeOrigin: true, ws: true, pathRewrite: {
                    '^/api': ''
                }
            }
        }
    }
});
