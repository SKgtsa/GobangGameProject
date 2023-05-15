// 防抖
export function _debounce(fn, delay) {
    delay = delay || 200;
    let timer;
    return () => {
        // @ts-ignore
        let th = this;
        let args = arguments;
        if (timer) {
            clearTimeout(timer);
        }
        timer = setTimeout(function () {
            timer = null;
            fn.apply(th, args);
        }, delay);
    };
}
// 节流
export function _throttle(fn, interval) {
    let last;
    let timer;
    interval = interval || 200;
    return () => {
        // @ts-ignore
        let th = this;
        let args = arguments;
        let now = +new Date();
        if (last && now - last < interval) {
            clearTimeout(timer);
            timer = setTimeout(function () {
                last = now;
                fn.apply(th, args);
            }, interval);
        } else {
            last = now;
            fn.apply(th, args);
        }
    }
}
