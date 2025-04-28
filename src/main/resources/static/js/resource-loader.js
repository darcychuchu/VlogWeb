/**
 * 资源加载器
 * 优先从CDN加载资源，如果CDN加载失败，则从本地加载
 */
class ResourceLoader {
    /**
     * 加载CSS资源
     * @param {string} cdnUrl - CDN资源URL
     * @param {string} localUrl - 本地资源URL
     * @param {number} timeout - 超时时间（毫秒）
     * @returns {Promise} - 加载结果Promise
     */
    static loadCSS(cdnUrl, localUrl, timeout = 3000) {
        return new Promise((resolve) => {
            // 创建link元素
            const link = document.createElement('link');
            link.rel = 'stylesheet';
            link.href = cdnUrl;

            // 设置超时计时器
            const timeoutId = setTimeout(() => {
                console.warn(`CDN CSS资源加载超时: ${cdnUrl}, 切换到本地资源: ${localUrl}`);
                link.href = localUrl;
            }, timeout);

            // 加载成功处理
            link.onload = () => {
                clearTimeout(timeoutId);
                console.log(`CSS资源加载成功: ${link.href}`);
                resolve(true);
            };

            // 加载失败处理
            link.onerror = () => {
                clearTimeout(timeoutId);
                console.warn(`CDN CSS资源加载失败: ${cdnUrl}, 切换到本地资源: ${localUrl}`);
                link.href = localUrl;

                // 本地资源加载失败处理
                link.onerror = () => {
                    console.error(`本地CSS资源加载失败: ${localUrl}`);
                    resolve(false);
                };
            };

            // 添加到文档
            document.head.appendChild(link);
        });
    }

    /**
     * 加载JavaScript资源
     * @param {string} cdnUrl - CDN资源URL
     * @param {string} localUrl - 本地资源URL
     * @param {number} timeout - 超时时间（毫秒）
     * @returns {Promise} - 加载结果Promise
     */
    static loadScript(cdnUrl, localUrl, timeout = 3000) {
        return new Promise((resolve) => {
            // 创建script元素
            const script = document.createElement('script');
            script.src = cdnUrl;

            // 设置超时计时器
            const timeoutId = setTimeout(() => {
                console.warn(`CDN JS资源加载超时: ${cdnUrl}, 切换到本地资源: ${localUrl}`);
                script.src = localUrl;
            }, timeout);

            // 加载成功处理
            script.onload = () => {
                clearTimeout(timeoutId);
                console.log(`JS资源加载成功: ${script.src}`);
                resolve(true);
            };

            // 加载失败处理
            script.onerror = () => {
                clearTimeout(timeoutId);
                console.warn(`CDN JS资源加载失败: ${cdnUrl}, 切换到本地资源: ${localUrl}`);
                script.src = localUrl;

                // 本地资源加载失败处理
                script.onerror = () => {
                    console.error(`本地JS资源加载失败: ${localUrl}`);
                    resolve(false);
                };
            };

            // 添加到文档
            document.head.appendChild(script);
        });
    }
}

// 不再使用DOMContentLoaded事件阻塞页面加载
// 改为直接在页面底部引入资源
