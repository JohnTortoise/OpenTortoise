/**
 * 通用分页组件
 * 支持移动端和桌面端的响应式布局
 * 提供页码导航、每页显示条数选择等功能
 */
class PaginationComponent {
    constructor(options = {}) {
        this.container = options.container;
        this.currentPage = options.currentPage || 1;
        this.pageSize = options.pageSize || 10;
        this.pageSizeOptions = options.pageSizeOptions || [5, 10, 20, 50];
        this.onPageChange = options.onPageChange;
        this.onPageSizeChange = options.onPageSizeChange;
        this.showPageSizeSelector = options.showPageSizeSelector !== false;

        this.init();
    }

    init() {
        if (!this.container) {
            console.error('Pagination container is required');
            return;
        }
    }

    /**
     * 渲染分页组件
     * @param {Object} pageData - 分页数据对象
     * @param {number} pageData.current - 当前页
     * @param {number} pageData.pages - 总页数
     * @param {number} pageData.size - 每页条数
     * @param {number} pageData.total - 总条数
     */
    render(pageData) {
        if (!pageData || !pageData.total || pageData.total === 0) {
            this.container.innerHTML = '';
            return;
        }

        const { current, pages, size, total } = pageData;
        this.currentPage = current;
        this.pageSize = size;

        const startItem = (current - 1) * size + 1;
        const endItem = Math.min(current * size, total);
        const displayStart = total > 0 ? startItem : 0;
        const displayEnd = total > 0 ? endItem : 0;

        // 生成页码按钮数组
        const pageNumbers = this.generatePageNumbers(current, pages);

        // 生成分页HTML
        let paginationHtml = `
            <div class="bg-white px-4 py-6 sm:px-6">
                <!-- 移动端分页 -->
                <div class="flex items-center justify-between sm:hidden">
                    <div class="text-sm text-gray-700">
                        ${displayStart}-${displayEnd} / ${total}
                    </div>
                    <div class="flex items-center space-x-1">
                        <button id="${this.getElementId('mobile-prev-btn')}"
                                class="p-2 rounded-lg border border-gray-200 text-gray-600 hover:bg-gray-50 hover:text-gray-900 transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
                            <i class="fas fa-chevron-left"></i>
                        </button>
                        <span class="px-3 py-2 text-sm text-gray-700">${current} / ${pages}</span>
                        <button id="${this.getElementId('mobile-next-btn')}"
                                class="p-2 rounded-lg border border-gray-200 text-gray-600 hover:bg-gray-50 hover:text-gray-900 transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
                            <i class="fas fa-chevron-right"></i>
                        </button>
                    </div>
                </div>

                <!-- 桌面端分页 -->
                <div class="hidden sm:flex sm:items-center sm:justify-between">
                    <!-- 左侧：数据信息和每页显示 -->
                    <div class="flex items-center space-x-6">
                        <div class="text-sm text-gray-700">
                            显示第 <span class="font-medium text-gray-900">${displayStart}</span> 到 <span class="font-medium text-gray-900">${displayEnd}</span> 条，共 <span class="font-medium text-gray-900">${total}</span> 条
                        </div>
                        ${this.showPageSizeSelector ? this.renderPageSizeSelector() : ''}
                    </div>

                    <!-- 右侧：页码导航 -->
                    <div class="flex items-center space-x-1">
                        <!-- 上一页按钮 -->
                        <button id="${this.getElementId('prev-btn')}"
                                class="p-2 rounded-lg border border-gray-200 text-gray-600 hover:bg-gray-50 hover:text-gray-900 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed">
                            <i class="fas fa-chevron-left"></i>
                        </button>

                        <!-- 页码按钮 -->
                        ${pageNumbers.map(pageNum => {
                            if (pageNum === '...') {
                                return `<span class="px-3 py-2 text-sm text-gray-400">...</span>`;
                            } else if (pageNum === current) {
                                return `<button class="px-3 py-2 text-sm font-medium bg-blue-600 text-white rounded-lg shadow-sm">${pageNum}</button>`;
                            } else {
                                return `<button class="page-btn px-3 py-2 text-sm text-gray-700 hover:bg-gray-100 hover:text-gray-900 rounded-lg transition-all duration-200"
                                        data-page="${pageNum}">${pageNum}</button>`;
                            }
                        }).join('')}

                        <!-- 下一页按钮 -->
                        <button id="${this.getElementId('next-btn')}"
                                class="p-2 rounded-lg border border-gray-200 text-gray-600 hover:bg-gray-50 hover:text-gray-900 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed">
                            <i class="fas fa-chevron-right"></i>
                        </button>
                    </div>
                </div>
            </div>
        `;

        this.container.innerHTML = paginationHtml;

        // 绑定事件
        this.bindEvents(pageData);
    }

    /**
     * 生成页码按钮数组
     */
    generatePageNumbers(current, totalPages) {
        const pages = [];
        const delta = 2; // 当前页前后显示的页数

        // 始终显示第一页
        if (1 < current - delta) {
            pages.push(1);
            if (2 < current - delta) {
                pages.push('...');
            }
        }

        // 显示当前页附近的页码
        for (let i = Math.max(1, current - delta); i <= Math.min(totalPages, current + delta); i++) {
            pages.push(i);
        }

        // 始终显示最后一页
        if (totalPages > current + delta) {
            if (totalPages - 1 > current + delta) {
                pages.push('...');
            }
            pages.push(totalPages);
        }

        return pages;
    }

    /**
     * 渲染每页显示条数选择器
     */
    renderPageSizeSelector() {
        return `
            <div class="flex items-center space-x-2">
                <span class="text-sm text-gray-700">每页</span>
                <div class="relative">
                    <div id="${this.getElementId('page-size-display')}"
                         class="inline-flex items-center px-3 py-1.5 text-sm border border-gray-200 rounded-lg bg-white hover:bg-gray-50 cursor-pointer transition-all duration-200 min-w-[60px] justify-between">
                        <span id="${this.getElementId('page-size-text')}">${this.pageSize}</span>
                        <i class="fas fa-chevron-down text-gray-400 ml-2 text-xs"></i>
                    </div>
                    <div id="${this.getElementId('page-size-dropdown')}"
                         class="absolute z-50 mt-1 w-full bg-white shadow-xl rounded-lg py-1 text-sm border border-gray-200 hidden">
                        ${this.pageSizeOptions.map(size => `
                            <div class="page-size-option px-3 py-2 hover:bg-blue-50 cursor-pointer transition-colors" data-value="${size}">
                                ${size}
                            </div>
                        `).join('')}
                    </div>
                    <input type="hidden" id="${this.getElementId('page-size')}" value="${this.pageSize}">
                </div>
                <span class="text-sm text-gray-700">条</span>
            </div>
        `;
    }

    /**
     * 绑定分页事件
     */
    bindEvents(pageData) {
        const { current, pages } = pageData;

        // 上一页按钮
        const prevBtn = document.getElementById(this.getElementId('prev-btn'));
        const mobilePrevBtn = document.getElementById(this.getElementId('mobile-prev-btn'));

        if (current <= 1) {
            if (prevBtn) this.disableButton(prevBtn);
            if (mobilePrevBtn) this.disableButton(mobilePrevBtn);
        } else {
            if (prevBtn) this.enableButton(prevBtn, () => this.handlePageChange(current - 1));
            if (mobilePrevBtn) this.enableButton(mobilePrevBtn, () => this.handlePageChange(current - 1));
        }

        // 下一页按钮
        const nextBtn = document.getElementById(this.getElementId('next-btn'));
        const mobileNextBtn = document.getElementById(this.getElementId('mobile-next-btn'));

        if (current >= pages) {
            if (nextBtn) this.disableButton(nextBtn);
            if (mobileNextBtn) this.disableButton(mobileNextBtn);
        } else {
            if (nextBtn) this.enableButton(nextBtn, () => this.handlePageChange(current + 1));
            if (mobileNextBtn) this.enableButton(mobileNextBtn, () => this.handlePageChange(current + 1));
        }

        // 页码按钮
        const pageBtns = this.container.querySelectorAll('.page-btn');
        pageBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                const page = parseInt(btn.getAttribute('data-page'));
                if (!isNaN(page) && page >= 1 && page <= pages) {
                    this.handlePageChange(page);
                }
            });
        });

        // 每页显示条数选择器
        if (this.showPageSizeSelector) {
            this.bindPageSizeEvents();
        }
    }

    /**
     * 绑定每页显示条数选择器事件
     */
    bindPageSizeEvents() {
        const pageSizeDisplay = document.getElementById(this.getElementId('page-size-display'));
        const pageSizeDropdown = document.getElementById(this.getElementId('page-size-dropdown'));
        const pageSizeText = document.getElementById(this.getElementId('page-size-text'));

        if (!pageSizeDisplay || !pageSizeDropdown) return;

        // 切换下拉菜单
        pageSizeDisplay.addEventListener('click', (e) => {
            e.stopPropagation();
            const isHidden = pageSizeDropdown.classList.contains('hidden');

            // 关闭其他下拉菜单
            document.querySelectorAll('.dropdown-menu').forEach(menu => {
                if (menu !== pageSizeDropdown) {
                    menu.classList.add('hidden');
                }
            });

            if (isHidden) {
                this.showDropdown(pageSizeDropdown);
            } else {
                this.hideDropdown(pageSizeDropdown);
            }
        });

        // 点击其他地方关闭下拉菜单
        document.addEventListener('click', (e) => {
            if (!e.target.closest(`#${this.getElementId('page-size-display')}`) &&
                !e.target.closest(`#${this.getElementId('page-size-dropdown')}`)) {
                this.hideDropdown(pageSizeDropdown);
            }
        });

        // 处理选项选择
        pageSizeDropdown.addEventListener('click', (e) => {
            const option = e.target.closest('.page-size-option');
            if (option) {
                const value = option.getAttribute('data-value');
                const numericValue = parseInt(value);

                if (numericValue !== this.pageSize) {
                    this.pageSize = numericValue;
                    pageSizeText.textContent = value;
                    this.handlePageSizeChange(numericValue);
                }

                this.hideDropdown(pageSizeDropdown);
            }
        });
    }

    /**
     * 处理页码变化
     */
    handlePageChange(page) {
        if (this.onPageChange) {
            this.onPageChange(page);
        }
    }

    /**
     * 处理每页显示条数变化
     */
    handlePageSizeChange(pageSize) {
        if (this.onPageSizeChange) {
            this.onPageSizeChange(pageSize);
        }
    }

    /**
     * 禁用按钮
     */
    disableButton(button) {
        if (button) {
            button.disabled = true;
            button.classList.add('opacity-50', 'cursor-not-allowed');
        }
    }

    /**
     * 启用按钮
     */
    enableButton(button, callback) {
        if (button) {
            button.disabled = false;
            button.classList.remove('opacity-50', 'cursor-not-allowed');
            button.onclick = callback;
        }
    }

    /**
     * 显示下拉菜单
     */
    showDropdown(dropdown) {
        if (dropdown) {
            dropdown.classList.remove('hidden');
            // 确保下拉菜单在视窗内
            const rect = dropdown.getBoundingClientRect();
            if (rect.bottom > window.innerHeight) {
                dropdown.style.top = 'auto';
                dropdown.style.bottom = '100%';
            }
        }
    }

    /**
     * 隐藏下拉菜单
     */
    hideDropdown(dropdown) {
        if (dropdown) {
            dropdown.classList.add('hidden');
            dropdown.style.top = '';
            dropdown.style.bottom = '';
        }
    }

    /**
     * 获取元素ID
     */
    getElementId(suffix) {
        const baseId = this.container.id || 'pagination';
        return `${baseId}-${suffix}`;
    }

    /**
     * 更新分页状态
     */
    updateState(currentPage, pageSize) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }
}

// 全局函数用于创建分页组件实例
function createPagination(options) {
    return new PaginationComponent(options);
}

// 简化版分页组件（用于其他页面）
function renderSimplePagination(container, pageData, onPageChange) {
    if (!container || !pageData || !pageData.total || pageData.total === 0) {
        container.innerHTML = '';
        return;
    }

    const { current, pages, total } = pageData;

    let paginationHtml = `
        <div class="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6">
            <div class="flex-1 flex justify-between sm:hidden">
                <button id="${container.id}-mobile-prev" class="relative inline-flex items-center px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 ${current <= 1 ? 'opacity-50 cursor-not-allowed' : ''}">
                    上一页
                </button>
                <div class="text-sm text-gray-700">
                    ${current} / ${pages}
                </div>
                <button id="${container.id}-mobile-next" class="relative ml-3 inline-flex items-center px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 ${current >= pages ? 'opacity-50 cursor-not-allowed' : ''}">
                    下一页
                </button>
            </div>
            <div class="hidden sm:flex sm:flex-1 sm:items-center sm:justify-between">
                <div>
                    <p class="text-sm text-gray-700">
                        显示第 <span class="font-medium">${(current - 1) * (pageData.size || 10) + 1}</span> 到 <span class="font-medium">${Math.min(current * (pageData.size || 10), total)}</span> 条，共 <span class="font-medium">${total}</span> 条
                    </p>
                </div>
                <div>
                    <nav class="relative z-0 inline-flex rounded-md shadow-sm -space-x-px" aria-label="Pagination">
                        <button id="${container.id}-prev" class="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 ${current <= 1 ? 'opacity-50 cursor-not-allowed' : ''}">
                            <i class="fas fa-chevron-left"></i>
                        </button>
                        <div class="relative inline-flex items-center px-4 py-2 border border-gray-300 bg-white text-sm font-medium text-gray-700">
                            第 <input type="number" id="${container.id}-page-input" class="w-12 text-center border-0 focus:ring-0" value="${current}" min="1" max="${pages}"> 页 / 共 ${pages} 页
                        </div>
                        <button id="${container.id}-next" class="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 ${current >= pages ? 'opacity-50 cursor-not-allowed' : ''}">
                            <i class="fas fa-chevron-right"></i>
                        </button>
                    </nav>
                </div>
            </div>
        </div>
    `;

    container.innerHTML = paginationHtml;

    // 绑定事件
    const bindButton = (id, callback) => {
        const btn = document.getElementById(id);
        if (btn && !btn.classList.contains('opacity-50')) {
            btn.onclick = callback;
        }
    };

    bindButton(`${container.id}-prev`, () => onPageChange(current - 1));
    bindButton(`${container.id}-mobile-prev`, () => onPageChange(current - 1));
    bindButton(`${container.id}-next`, () => onPageChange(current + 1));
    bindButton(`${container.id}-mobile-next`, () => onPageChange(current + 1));

    // 页码输入框事件
    const pageInput = document.getElementById(`${container.id}-page-input`);
    if (pageInput) {
        pageInput.addEventListener('change', (e) => {
            const page = parseInt(e.target.value);
            if (page >= 1 && page <= pages) {
                onPageChange(page);
            } else {
                e.target.value = current;
            }
        });

        pageInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                const page = parseInt(e.target.value);
                if (page >= 1 && page <= pages) {
                    onPageChange(page);
                } else {
                    e.target.value = current;
                }
            }
        });
    }
}

