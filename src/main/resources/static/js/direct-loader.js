/**
 * 直接加载器 - 在页面底部直接加载资源
 * 不阻塞页面渲染，让页面内容先展示出来
 */

// 加载CSS资源
function loadCSS(url) {
    const link = document.createElement('link');
    link.rel = 'stylesheet';
    link.href = url;
    
    link.onerror = () => {
        console.error(`CSS资源加载失败: ${url}`);
    };
    
    document.head.appendChild(link);
    return link;
}

// 加载JavaScript资源
function loadScript(url, async = true, defer = true) {
    const script = document.createElement('script');
    script.src = url;
    script.async = async;
    script.defer = defer;
    
    script.onerror = () => {
        console.error(`JS资源加载失败: ${url}`);
    };
    
    document.body.appendChild(script);
    return script;
}

// 加载常用CDN资源
function loadCommonResources() {
    // 加载Tailwind CSS
    loadCSS('https://cdn.tailwindcss.com/3.3.3');
    
    // 加载Font Awesome
    loadCSS('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css');
    
    // 加载jQuery (如果需要)
    // loadScript('https://code.jquery.com/jquery-3.7.0.min.js');
    
    // 加载Bootstrap (如果需要)
    // loadCSS('https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css');
    // loadScript('https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js');
    
    console.log('所有资源已开始加载，页面可以正常显示');
}

// 页面加载完成后执行，但不阻塞页面渲染
window.addEventListener('load', function() {
    loadCommonResources();
});

// 如果需要立即加载，可以直接调用
// loadCommonResources();
