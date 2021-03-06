(function($) {
var OPT_NAME = "calendar-opt";
//......................................................................................
// 帮助函数集合
var util = {
    opt: function(ele) {
        return util.selection(ele).data(OPT_NAME);
    },
    selection: function(ele) {
        var jq = $(ele);
        if(jq.hasClass("calendar"))
            return jq.parent();
        var p = jq.parents(".calendar");
        if(p.size() > 0)
            return p.parent();
        return jq.children(".calendar").parent();
    },
    moveMonth: function(yy, mm, off) {
        mm += off;
        if(mm < 1) {
            return {
                year: yy - 1,
                month: 12
            };
        } else if(mm > 12) {
            return {
                year: yy + 1,
                month: 1
            };
        }
        return {
            year: yy,
            month: mm
        }
    },
    doSwitch: function(off) {
        var selection = util.selection(this);
        var opt = util.opt(selection);
        var calBody = selection.find(".cal_body");
        var yy = calBody.attr("yy") * 1;
        var mm = calBody.attr("mm") * 1;
        var d = util.moveMonth(yy, mm, off);
        var html = dom.cal_table(opt, d.year, d.month);
        calBody.empty().html(html);
        calBody.attr("yy", d.year);
        calBody.attr("mm", d.month);
    }
};
//.........................................................................
// HTML 生成相关方法
var dom = {
    month: function(opt, yy, mm) {
        var chckd = z.d(opt.date);
        var today = z.today();
        var dd = z.monthDays(yy, mm);
        var html = '<table class="cal_mm" yy="' + yy + '" mm="' + mm + '" ';
        html += ' cellspacing="0" cellpadding="0">';
        html += '<thead><tr><th colspan="7" class="cal_mm_head">';
        html += z.msg("d.MM." + mm) + "&nbsp;&nbsp;" + yy;
        html += '</th></tr></thead>';
        html += '<tbody><tr class="cal_ww">';

        for(var x = 1; x <= 7; x++) {
            html += '<th>' + z.msg("d.w." + x) + '</th>';
        }
        html += '</tr><tr>';

        var i = 0;
        for(; i < dd.length; i++) {
            if(dd[i] == 1)
                break;
            html += '<td class="cal_dd cal_dd_out">&nbsp;</td>';
        }
        for(; i < dd.length; i++) {
            var todayCss = (yy == today.year && mm == today.month && dd[i] == today.date ? " cal_today" : "");
            var chckdCss = (yy == chckd.year && mm == chckd.month && dd[i] == chckd.date ? " cal_checked" : "");
            html += '<td class="cal_dd ' + todayCss + chckdCss + '">' + dd[i] + '</td>';
            if(i > 0 && (i + 1) % 7 == 0) {
                html += '</tr><tr>';
            }
        }
        html += '</tr></tbody></table>';
        return html;
    },
    cal_table: function(opt, yy, mm) {
        var html = '<table class="cal_table" cellspacing="1" cellpadding="2" align="center"><tr valign="top">';
        html += '<td class="cal_switch"><b class="cal_switch_prev"></b></td>';
        for(var i = 0; i < opt.range.length; i++) {
            var d = util.moveMonth(yy, mm, opt.range[i]);
            html += '<td class="cal_table_month">';
            html += dom.month(opt, d.year, d.month);
            html += '</td>';
        }
        html += '<td valign="top" class="cal_switch"><b class="cal_switch_next"></b></td>';
        html += '</tr></table>';

        return html;
    },
    init: function(opt) {
        var d = z.d(opt.date);
        var html = '<div class="calendar">';
        html += '<div class="cal_body" yy="' + d.year + '" mm="' + d.month + '" date="' + opt.date + '">';
        html += dom.cal_table(opt, d.year, d.month);
        html += '</div></div>';

        // 显示并返回
        return $(html).appendTo(this.empty());
    },
    depose: function() {
        var selection = util.selection(this);
        selection.undelegate().remove();
    }
};
//.........................................................................
var events = {
    bind: function(opt) {
        this.delegate(".cal_switch_prev", "click", events.onPrev);
        this.delegate(".cal_switch_next", "click", events.onNext);
        this.delegate(".cal_dd", "click", events.onClick);
    },
    onClick: function() {
        // 准备必要变量
        var opt = util.opt(this);
        var cbody = util.selection(this).find(".cal_body");
        // 得到当前日期
        var ds = z.alignr(cbody.attr("yy"), 4, "0");
        ds += "-" + z.alignr(cbody.attr("mm"), 2, "0");
        ds += '-' + z.alignr($(this).text(), 2, "0");
        // 调用回调
        if( typeof opt.click == "function") {
            if(false == opt.click.apply(this, [ds]))
                return;
        }
        // 改变当前选择的日期
        cbody.attr("date", ds);
        cbody.find(".cal_checked").removeClass("cal_checked");
        $(this).addClass("cal_checked");
    },
    onPrev: function() {
        util.doSwitch.apply(this, [-1]);
    },
    onNext: function() {
        util.doSwitch.apply(this, [1]);
    }
};
//.........................................................................
var commands = {
    depose: function() {
        dom.depose.apply(this);
    },
    getstr: function() {
        return this.find(".cal_body").attr("date");
    },
    get: function() {
        return z.d(this.find(".cal_body").attr("date"));
    }
};
//.........................................................................
// 在选区内绘制某一个或者几个月的日历
// opt.click - callback(ds){this 为 .cal_dd ...}，ds 格式为 "yyyy-MM-dd"，如果 return false，则不更改 .cal_checked
// opt.date  - 选中的当前日期，格式为 yyyy-MM-dd
// opt.range - 显示几个月，默认为 [0]，[-1, 0, 1] 表示前一个月，本月，后一个月
$.fn.extend({
    calendar: function(opt) {
        opt = opt || {};
        // 初始化模式
        if( typeof opt == "object") {
            opt.range = opt.range || [0];
            opt.date = opt.date || z.dstr(z.today());
            this.data(OPT_NAME, opt);
            // 初始化 DOM
            var div = dom.init.apply(this, [opt]);
            // 绑定事件
            events.bind.apply(this);
        }
        // 命令模式
        else if( typeof opt == "string") {
            if("function" != typeof commands[opt])
                throw "$.pop: don't support command '" + opt + "'";
            var re = commands[opt].apply(this);
            return re || this;
        }
        // 返回支持链式赋值
        return this;
    }
});
})(window.jQuery);
