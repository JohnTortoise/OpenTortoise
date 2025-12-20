/**
 * 统一的 Toast 提示组件
 * 使用方法：
 *   showToast('消息内容', true, 3000)  // 成功提示，3秒后自动关闭
 *   showToast('错误消息', false, 3000) // 错误提示，3秒后自动关闭
 *   showToast('消息内容', true)        // 使用默认时长（3秒）
 */

(function() {
    'use strict';

    // Toast 配置
    const TOAST_CONFIG = {
        defaultDuration: 3000,
        animationDuration: 300,
        position: {
            top: '2rem',
            right: '2rem'
        },
        zIndex: 10000
    };

    // 创建 Toast HTML 结构
    function createToastHTML() {
        const toastContainer = document.createElement('div');
        toastContainer.id = 'toast';
        toastContainer.className = 'toast-container';
        toastContainer.innerHTML = `
            <div class="toast-content">
                <div class="toast-icon" id="toastIcon"></div>
                <div class="toast-message-wrapper">
                    <p class="toast-message" id="toastMessage"></p>
                </div>
                <button class="toast-close" id="toastClose" aria-label="关闭">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        `;
        return toastContainer;
    }

    // 注入 Toast 样式
    function injectToastStyles() {
        if (document.getElementById('toast-styles')) {
            return; // 样式已存在，不重复注入
        }

        const style = document.createElement('style');
        style.id = 'toast-styles';
        style.textContent = `
            /* Toast 容器 */
            .toast-container {
                position: fixed;
                top: ${TOAST_CONFIG.position.top};
                right: ${TOAST_CONFIG.position.right};
                z-index: ${TOAST_CONFIG.zIndex};
                min-width: 300px;
                max-width: 450px;
                opacity: 0;
                visibility: hidden;
                transform: translateX(100%);
                transition: all ${TOAST_CONFIG.animationDuration}ms cubic-bezier(0.4, 0, 0.2, 1);
            }

            .toast-container.show {
                opacity: 1;
                visibility: visible;
                transform: translateX(0);
            }

            /* Toast 悬停效果 */
            .toast-container:hover .toast-content {
                transform: translateY(-2px);
            }

            .toast-container:hover .toast-content.success {
                box-shadow: 0 12px 30px rgba(59, 130, 246, 0.4);
            }

            .toast-container:hover .toast-content.error {
                box-shadow: 0 12px 30px rgba(239, 68, 68, 0.4);
            }

            .toast-container:hover .toast-content.warning {
                box-shadow: 0 12px 30px rgba(245, 158, 11, 0.4);
            }

            .toast-container:hover .toast-content.info {
                box-shadow: 0 12px 30px rgba(59, 130, 246, 0.4);
            }

            /* Toast 内容 */
            .toast-content {
                display: flex;
                align-items: center;
                padding: 1rem 1.25rem;
                border-radius: 1rem;
                box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
                background: white;
                border: none;
                transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                position: relative;
                overflow: hidden;
            }

            .toast-content::before {
                content: '';
                position: absolute;
                top: 0;
                left: 0;
                width: 4px;
                height: 100%;
                transition: all 0.3s ease;
            }

            /* Toast 图标 */
            .toast-icon {
                margin-right: 0.75rem;
                font-size: 1.5rem;
                flex-shrink: 0;
                display: flex;
                align-items: center;
                justify-content: center;
                width: 2rem;
                height: 2rem;
                border-radius: 50%;
                background: rgba(255, 255, 255, 0.2);
            }

            /* Toast 消息 */
            .toast-message-wrapper {
                flex: 1;
                min-width: 0;
            }

            .toast-message {
                font-size: 0.9375rem;
                font-weight: 600;
                margin: 0;
                color: #1f2937;
                word-wrap: break-word;
                line-height: 1.5;
            }

            /* Toast 关闭按钮 */
            .toast-close {
                margin-left: 0.75rem;
                background: none;
                border: none;
                color: #9ca3af;
                cursor: pointer;
                padding: 0.25rem;
                display: flex;
                align-items: center;
                justify-content: center;
                transition: color 0.2s;
                flex-shrink: 0;
            }

            .toast-close:hover {
                color: #4b5563;
            }

            /* 成功样式 - 使用系统蓝色主题 */
            .toast-content.success {
                background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
                box-shadow: 0 10px 25px rgba(59, 130, 246, 0.3);
                color: white;
            }

            .toast-content.success::before {
                background: rgba(255, 255, 255, 0.3);
            }

            .toast-content.success .toast-icon {
                color: white;
                background: rgba(255, 255, 255, 0.2);
            }

            .toast-content.success .toast-message {
                color: white;
            }

            .toast-content.success .toast-close {
                color: rgba(255, 255, 255, 0.8);
            }

            .toast-content.success .toast-close:hover {
                color: white;
            }

            /* 错误样式 - 使用红色渐变 */
            .toast-content.error {
                background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
                box-shadow: 0 10px 25px rgba(239, 68, 68, 0.3);
                color: white;
            }

            .toast-content.error::before {
                background: rgba(255, 255, 255, 0.3);
            }

            .toast-content.error .toast-icon {
                color: white;
                background: rgba(255, 255, 255, 0.2);
            }

            .toast-content.error .toast-message {
                color: white;
            }

            .toast-content.error .toast-close {
                color: rgba(255, 255, 255, 0.8);
            }

            .toast-content.error .toast-close:hover {
                color: white;
            }

            /* 警告样式 - 使用橙色渐变 */
            .toast-content.warning {
                background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
                box-shadow: 0 10px 25px rgba(245, 158, 11, 0.3);
                color: white;
            }

            .toast-content.warning::before {
                background: rgba(255, 255, 255, 0.3);
            }

            .toast-content.warning .toast-icon {
                color: white;
                background: rgba(255, 255, 255, 0.2);
            }

            .toast-content.warning .toast-message {
                color: white;
            }

            .toast-content.warning .toast-close {
                color: rgba(255, 255, 255, 0.8);
            }

            .toast-content.warning .toast-close:hover {
                color: white;
            }

            /* 信息样式 - 使用系统蓝色主题 */
            .toast-content.info {
                background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 50%, #1e40af 100%);
                box-shadow: 0 10px 25px rgba(59, 130, 246, 0.3);
                color: white;
            }

            .toast-content.info::before {
                background: rgba(255, 255, 255, 0.3);
            }

            .toast-content.info .toast-icon {
                color: white;
                background: rgba(255, 255, 255, 0.2);
            }

            .toast-content.info .toast-message {
                color: white;
            }

            .toast-content.info .toast-close {
                color: rgba(255, 255, 255, 0.8);
            }

            .toast-content.info .toast-close:hover {
                color: white;
            }
        `;
        document.head.appendChild(style);
    }

    // 初始化 Toast
    function initToast() {
        // 如果 toast 已存在，直接返回
        if (document.getElementById('toast')) {
            return;
        }

        // 注入样式
        injectToastStyles();

        // 创建并添加 Toast DOM
        const toastContainer = createToastHTML();
        document.body.appendChild(toastContainer);

        // 绑定关闭按钮事件
        const closeBtn = document.getElementById('toastClose');
        if (closeBtn) {
            closeBtn.addEventListener('click', hideToast);
        }
    }

    // 显示 Toast
    function showToast(message, type = true, duration = TOAST_CONFIG.defaultDuration) {
        // 确保 Toast 已初始化
        initToast();

        const toast = document.getElementById('toast');
        if (!toast) {
            return;
        }

        const toastContent = toast.querySelector('.toast-content');
        const toastMessage = document.getElementById('toastMessage');
        const toastIcon = document.getElementById('toastIcon');

        if (!toastContent || !toastMessage || !toastIcon) {
            return;
        }

        // 设置消息内容
        toastMessage.textContent = message || '';

        // 确定类型（兼容旧的 isSuccess 参数）
        let toastType = 'success';
        if (type === false) {
            toastType = 'error';
        } else if (typeof type === 'string') {
            toastType = type; // 支持 'success', 'error', 'warning', 'info'
        }

        // 移除所有类型类
        toastContent.classList.remove('success', 'error', 'warning', 'info');

        // 设置图标和样式
        let iconClass = '';
        switch (toastType) {
            case 'success':
                iconClass = 'fas fa-check-circle';
                toastContent.classList.add('success');
                break;
            case 'error':
                iconClass = 'fas fa-exclamation-circle';
                toastContent.classList.add('error');
                break;
            case 'warning':
                iconClass = 'fas fa-exclamation-triangle';
                toastContent.classList.add('warning');
                break;
            case 'info':
                iconClass = 'fas fa-info-circle';
                toastContent.classList.add('info');
                break;
            default:
                iconClass = 'fas fa-check-circle';
                toastContent.classList.add('success');
        }

        toastIcon.innerHTML = `<i class="${iconClass}"></i>`;

        // 清除之前的定时器
        if (toast.dataset.timeoutId) {
            clearTimeout(parseInt(toast.dataset.timeoutId));
        }

        // 显示 Toast
        requestAnimationFrame(() => {
            toast.classList.add('show');
        });

        // 自动隐藏
        if (duration > 0) {
            const timeoutId = setTimeout(() => {
                hideToast();
            }, duration);
            toast.dataset.timeoutId = timeoutId.toString();
        }
    }

    // 隐藏 Toast
    function hideToast() {
        const toast = document.getElementById('toast');
        if (!toast) {
            return;
        }

        // 清除定时器
        if (toast.dataset.timeoutId) {
            clearTimeout(parseInt(toast.dataset.timeoutId));
            delete toast.dataset.timeoutId;
        }

        // 隐藏动画
        toast.classList.remove('show');

        // 延迟移除 DOM（可选，如果希望保留 DOM 可以注释掉）
        // setTimeout(() => {
        //     if (!toast.classList.contains('show')) {
        //         toast.style.display = 'none';
        //     }
        // }, TOAST_CONFIG.animationDuration);
    }

    // 导出到全局
    window.showToast = showToast;
    window.hideToast = hideToast;

    // 便捷方法
    window.showSuccessToast = function(message, duration) {
        showToast(message, 'success', duration);
    };

    window.showErrorToast = function(message, duration) {
        showToast(message, 'error', duration);
    };

    window.showWarningToast = function(message, duration) {
        showToast(message, 'warning', duration);
    };

    window.showInfoToast = function(message, duration) {
        showToast(message, 'info', duration);
    };

    // DOM 加载完成后自动初始化
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initToast);
    } else {
        initToast();
    }
})();

