// Local-only dependency build. This never invokes a client or server and never publishes.
const fs=require('node:fs'),path=require('node:path'),cp=require('node:child_process');
const task=process.argv[2];
if(!['runData','jar'].includes(task))throw new Error('Allowed tasks: runData (data generation only), jar');
const cwd=path.resolve(__dirname,'../../.thaumaturge-1.21.1-upstream');
if(!fs.existsSync(path.join(cwd,'gradlew.bat')))throw new Error('Check out the official Thaumaturge 1.21.1 branch beside this project.');
const logfile=path.resolve(__dirname,`../validation/upstream-${task}.log`);
fs.mkdirSync(path.dirname(logfile),{recursive:true});
const log=fs.createWriteStream(logfile);
const child=cp.spawn('cmd.exe',['/d','/c','gradlew.bat',task,'--console=plain','--no-configuration-cache'],{
    cwd,env:{...process.env,JAVA_TOOL_OPTIONS:[process.env.JAVA_TOOL_OPTIONS||'','-Dfile.encoding=UTF-8'].join(' ')},windowsHide:true,stdio:['ignore','pipe','pipe']});
console.log(`Thaumaturge local ${task}; log: ${logfile}`);
child.stdout.on('data',chunk=>log.write(chunk));child.stderr.on('data',chunk=>log.write(chunk));
child.on('close',code=>log.end(()=>{console.log(fs.readFileSync(logfile,'utf8').slice(-10000));process.exitCode=code||0;}));
child.on('error',error=>{console.error(error);process.exitCode=1;});
