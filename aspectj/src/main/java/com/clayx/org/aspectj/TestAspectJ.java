//切面函数，切下getView方法
//监听getView的执行时间
package com.clayx.org.aspectj;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.util.Log;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.annotation.Aspect;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
@Aspect
public class TestAspectJ {

   private static final String METHOD_EXECUTION = "execution(* *..MainActivity+.onCreate(..))";
//精确查找
   private static final String METHOD_CALL = "call(* *..AdapterComment+.getView(..))";

   private String TAG = "TEST";
    @Pointcut(METHOD_EXECUTION)
   public void methodExecution() {
  }

   @Pointcut(METHOD_CALL)
   public void methodCall() {

   }
    @Around("methodCall()")
    public Object weaveJoinPoint(ProceedingJoinPoint joinPoint) throws Throwable {

        if (currentObject == null){
            currentObject = joinPoint.getTarget();
        }
        //初始化计时器
        final StopWatch stopWatch = new StopWatch();
        //开始监听
        stopWatch.start();
        //调用原方法的执行。
        Object result = joinPoint.proceed();
        //监听结束
        stopWatch.stop();
        //获取方法信息对象
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String className;
        //获取当前对象，通过反射获取类别详细信息
        className = joinPoint.getThis().getClass().getName();

        String methodName = methodSignature.getName();
        String msg =  buildLogMessage(methodName, stopWatch.getTotalTime(1));
        if (currentObject != null && currentObject.equals(joinPoint.getTarget())){
            DebugLog.log(new MethodMsg(className,msg,stopWatch.getTotalTime(1)));
        }else if(currentObject != null && !currentObject.equals(joinPoint.getTarget())){
            DebugLog.log(new MethodMsg(className, msg,stopWatch.getTotalTime(1)));
			//error
            Log.e(className,msg);
            currentObject = joinPoint.getTarget();
//        DebugLog.outPut(new Path());    //日志存储
//        DebugLog.ReadIn(new Path());    //日志读取
        }
        return result;
    }

    @After("methodCall()")
    public void onCreateAfter(JoinPoint joinPoint) throws Throwable{
        Log.e("onCreateAfter:","onCreate is end .");
    }
    /**
     * 在截获的目标方法调用之前执行该Advise
     * @param joinPoint
     * @throws Throwable
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Before("methodCall()")
    public void onCreateBefore(JoinPoint joinPoint) throws Throwable{
        Activity activity = null;
        //获取目标对象
        activity = ((Activity)joinPoint.getTarget());
        //插入自己的实现，控制目标对象的执行
        ChooseDialog dialog = new ChooseDialog(activity);
        dialog.show();
    }
    /*创建日志信息
     * @param methodName 方法名
     * @param methodDuration 执行时间
     * 可计算出T
     */
    private static String buildLogMessage(String methodName, double methodDuration) {
        StringBuilder message = new StringBuilder();
        message.append(methodName);
        message.append(" --> ");
        message.append("[");
        message.append(methodDuration);
        if (StopWatch.Accuracy == 1){
            message.append("ms");
        }else {
            message.append("mic");
        }
        message.append("]      \n");

        return message.toString();
    }


}
