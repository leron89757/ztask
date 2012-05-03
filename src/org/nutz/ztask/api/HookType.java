package org.nutz.ztask.api;

/**
 * 钩子的类型
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public enum HookType {

	/**
	 * 当一个任务的标签被更改后调用
	 * <p>
	 * refer 为 String[]，表旧 labels
	 */
	LABEL,

	/**
	 * 当一个任务的计划日期被修改后调用
	 * <p>
	 * refer 表旧的任务计划时间
	 */
	PLAN_AT,

	/**
	 * 当被增加或者修改了注释后
	 * <p>
	 * refer 为 int，表修改的 command 的下标，-1 表示新增
	 */
	COMMENT,

	/**
	 * 当一个任务的 owner 被主动改变时，调用。（被动改变不会被调用）
	 * <p>
	 * refer 为 String，表旧 owner
	 */
	OWNER,

	/**
	 * 当一个任务内容被更新后调用
	 * <p>
	 * refer 为 String，表旧 text
	 */
	UPDATE,

	/**
	 * 当一个任务重新开始后，调用
	 * <p>
	 * refer 为 null
	 */
	RESTART,

	/**
	 * 当一个任务挂起后调用
	 * <p>
	 * refer 为 null
	 */
	HUNGUP,

	/**
	 * 当一个任务压栈后调用
	 * <p>
	 * refer 为 String，表旧 stack
	 */
	PUSH,

	/**
	 * 当一个任务完成后调用 (用 done==true 的方式弹栈)
	 * <p>
	 * refer 为 String，表旧 stack
	 */
	DONE,

	/**
	 * 当一个任务被拒绝后调用 (用 done==false 的方式弹栈)
	 * <p>
	 * refer 为 String，表旧 stack
	 */
	REJECT,

	/**
	 * 当一个任务被创建后调用
	 * <p>
	 * refer 为 null
	 */
	CREATE,

	/**
	 * 当一个任务被删除前调用
	 * <p>
	 * refer 为 null
	 */
	DROP

}
