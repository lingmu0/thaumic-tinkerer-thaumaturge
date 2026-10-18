const fs=require('node:fs'),path=require('node:path'),cp=require('node:child_process');
const version=process.argv[2];
if(!['1.21.1','26.1.2'].includes(version))throw new Error('Usage: node tools/build.cjs <1.21.1|26.1.2> [compileJava|build]');
const task=process.argv[3]||'build';
if(!['compileJava','build','clean','processResources','check'].includes(task))throw new Error('Unsupported task');
const root=path.resolve(__dirname,'..'),cwd=path.join(root,'neoforge-'+version);
const logfile=path.join(cwd,'build-port.log');
const log=fs.createWriteStream(logfile);
const env={...process.env};
env.JAVA_TOOL_OPTIONS=[env.JAVA_TOOL_OPTIONS||'','-Dfile.encoding=UTF-8 -Duser.language=en -Duser.country=US'].join(' ');
const program=process.platform==='win32'?'cmd.exe':path.join(cwd,'gradlew');
const args=process.platform==='win32'?['/d','/c','gradlew.bat',task,'--console=plain','--no-configuration-cache']:[task,'--console=plain','--no-configuration-cache'];
console.log(`Building ${version}: ${task}. Full log: ${logfile}`);
const child=cp.spawn(program,args,{cwd,env,windowsHide:true,stdio:['ignore','pipe','pipe']});
child.stdout.on('data',data=>log.write(data));child.stderr.on('data',data=>log.write(data));
child.on('error',error=>{log.end(String(error));process.exitCode=1;});
child.on('close',code=>log.end(()=>{
    const text=fs.readFileSync(logfile,'utf8');
    if(code) {
        const lines=text.split('FAILURE:')[0].split('\n'),selected=[];
        for(let index=0;index<lines.length;index++)if(/error:|ERROR:/.test(lines[index]))selected.push(...lines.slice(Math.max(0,index),Math.min(lines.length,index+7)));
        console.log(selected.length?selected.join('\n'):text.slice(-16000));
    } else console.log(text.slice(-2500));
    process.exitCode=code||0;
}));
