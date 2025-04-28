/**
 * 备用资源加载器
 * 当CDN资源加载失败时，自动切换到本地资源
 */

// 检查Tailwind CSS是否加载成功
function checkTailwind() {
    // 如果页面上没有Tailwind的样式，加载本地版本
    if (!window.tailwind && !document.querySelector('style[data-tailwind]')) {
        console.warn('Tailwind CSS 加载失败，使用本地版本');
        loadScript('/static/js/tailwind.js');
    }
}

// 检查Font Awesome是否加载成功
function checkFontAwesome() {
    // 尝试获取一个Font Awesome图标
    const testIcon = document.createElement('i');
    testIcon.className = 'fas fa-check';
    testIcon.style.display = 'none';
    document.body.appendChild(testIcon);
    
    // 获取计算样式
    const computedStyle = window.getComputedStyle(testIcon);
    const fontFamily = computedStyle.getPropertyValue('font-family');
    
    // 如果没有正确加载Font Awesome，加载本地版本
    if (!fontFamily.includes('Font Awesome')) {
        console.warn('Font Awesome 加载失败，使用本地版本');
        loadCSS('/static/css/font-awesome/all.min.css');
    }
    
    // 移除测试元素
    document.body.removeChild(testIcon);
}

// 加载CSS
function loadCSS(url) {
    const link = document.createElement('link');
    link.rel = 'stylesheet';
    link.href = url;
    document.head.appendChild(link);
}

// 加载JavaScript
function loadScript(url) {
    const script = document.createElement('script');
    script.src = url;
    document.body.appendChild(script);
}

// 页面加载完成后检查资源
window.addEventListener('load', function() {
    // 延迟检查，给CDN资源一些加载时间
    setTimeout(function() {
        checkTailwind();
        checkFontAwesome();
    }, 2000);
});
