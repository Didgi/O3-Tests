package metrics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

public final class MetricsDashboardGenerator {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Path METRICS = Path.of("target", "metrics");

    private MetricsDashboardGenerator() { }

    public static void main(String[] args) throws IOException {
        Path directory = args.length > 0 ? Path.of(args[0]) : METRICS;
        Path output = args.length > 1 ? Path.of(args[1]) : directory.resolve("dashboard/index.html");
        JsonNode inventory = readRequired(directory.resolve("test-inventory.json"));
        JsonNode execution = readRequired(directory.resolve("test-run.json"));
        JsonNode readiness = readRequired(directory.resolve("release-readiness.json"));
        validate(inventory, execution, readiness);

        ObjectNode payload = MAPPER.createObjectNode();
        payload.set("inventory", inventory);
        payload.set("execution", execution);
        payload.set("readiness", readiness);
        Path historyPath = directory.resolve("history.json");
        JsonNode history = Files.isRegularFile(historyPath)
                ? MAPPER.readTree(historyPath.toFile()) : MAPPER.createArrayNode();
        if (!history.isArray()) {
            throw new IllegalArgumentException("history.json must be an array of CI runs");
        }
        payload.set("history", history);
        Path apiCoveragePath = directory.resolve("api-coverage.json");
        JsonNode apiCoverage = Files.isRegularFile(apiCoveragePath)
                ? MAPPER.readTree(apiCoveragePath.toFile()) : MAPPER.createObjectNode();
        if (!apiCoverage.isObject()) {
            throw new IllegalArgumentException("api-coverage.json must be an object");
        }
        payload.set("apiCoverage", apiCoverage);
        Path flakyPath = directory.resolve("flaky-metrics.json");
        JsonNode flakyMetrics = Files.isRegularFile(flakyPath)
                ? MAPPER.readTree(flakyPath.toFile()) : MAPPER.createObjectNode();
        if (!flakyMetrics.isObject()) {
            throw new IllegalArgumentException("flaky-metrics.json must be an object");
        }
        payload.set("flakyMetrics", flakyMetrics);
        Path repairPath = directory.resolve("test-repair-metrics.json");
        JsonNode repairMetrics = Files.isRegularFile(repairPath)
                ? MAPPER.readTree(repairPath.toFile()) : MAPPER.createObjectNode();
        payload.set("repairMetrics", repairMetrics);
        Path prPath = directory.resolve("pr-approval-metrics.json");
        JsonNode prMetrics = Files.isRegularFile(prPath)
                ? MAPPER.readTree(prPath.toFile()) : MAPPER.createObjectNode();
        payload.set("prMetrics", prMetrics);
        payload.put("generatedAt", Instant.now().toString());

        // Escape HTML-significant characters to prevent breaking out of the script tag.
        String data = MAPPER.writeValueAsString(payload)
                .replace("<", "\\u003c")
                .replace(">", "\\u003e")
                .replace("&", "\\u0026")
                .replace("\u2028", "\\u2028")
                .replace("\u2029", "\\u2029");
        Files.createDirectories(output.toAbsolutePath().getParent());
        Files.writeString(output, TEMPLATE.replace("__METRICS_PAYLOAD__", data), StandardCharsets.UTF_8);
        System.out.println("Dashboard created: " + output.toAbsolutePath());
    }

    private static JsonNode readRequired(Path file) throws IOException {
        if (!Files.isRegularFile(file)) {
            throw new IOException("Required metrics file is missing: " + file.toAbsolutePath());
        }
        return MAPPER.readTree(file.toFile());
    }

    private static void validate(JsonNode inventory, JsonNode execution, JsonNode readiness) {
        if (!inventory.path("tests").isArray() || !execution.path("tests").isArray()
                || !readiness.path("criticalTests").isObject()) {
            throw new IllegalArgumentException("Unexpected metrics JSON schema");
        }
        int invTotal = inventory.path("summary").path("total").asInt(-1);
        int runTotal = execution.path("summary").path("total").asInt(-2);
        if (invTotal < 0 || invTotal != runTotal) {
            throw new IllegalArgumentException("Inventory and execution test counts differ: " + invTotal + " / " + runTotal);
        }
    }

    private static final String TEMPLATE = """
<!doctype html>
<html lang="ru">
<head>
<meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>OpenMRS · QA Dashboard</title>
<style>
:root{color-scheme:dark;--bg:#0b1020;--panel:#141d31;--border:#29354c;--text:#f2f5fb;--muted:#9eacc5;--green:#35d39c;--red:#ff6c79;--yellow:#f6c56b;--blue:#74b8ff}
*{box-sizing:border-box}body{margin:0;background:var(--bg);color:var(--text);font:15px/1.5 system-ui,-apple-system,Segoe UI,sans-serif}main{max-width:1180px;margin:auto;padding:32px 22px 70px}h1{font-size:32px;letter-spacing:-.04em;margin:0}h2{font-size:20px;margin:0 0 14px}h3{font-size:15px;margin:0 0 8px}
.sub,.muted{color:var(--muted)}.sub{margin:5px 0 25px}.section{background:var(--panel);border:1px solid var(--border);border-radius:15px;padding:22px;margin-top:17px}.grid{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:12px}.card{background:#0d1527;border:1px solid var(--border);border-radius:12px;padding:16px;min-width:0}.label{font-size:13px;color:var(--muted)}.value{font-size:29px;font-weight:750;letter-spacing:-.04em;margin-top:6px}.detail{color:var(--muted);font-size:12px;margin-top:3px}.two{display:grid;grid-template-columns:1fr 1fr;gap:16px}.status{font-weight:800;font-size:25px;letter-spacing:.03em}.ready{color:var(--green)}.not-ready{color:var(--red)}.unknown{color:var(--yellow)}.reason{border-left:3px solid var(--red);background:#241e30;padding:9px 12px;margin-top:8px;border-radius:4px}.warning{border-left-color:var(--yellow)}.bars{display:flex;height:14px;border-radius:9px;overflow:hidden;background:#33425b;margin:17px 0}.bars>span{min-width:0}.legend{display:flex;gap:14px;flex-wrap:wrap;font-size:13px;color:var(--muted)}.dot{display:inline-block;width:9px;height:9px;border-radius:50%;margin-right:6px}.info{display:inline-grid;place-items:center;border:1px solid #7385a5;border-radius:50%;width:17px;height:17px;font:700 11px system-ui;color:#c5d2e9;cursor:help;margin-left:6px;position:relative;vertical-align:middle}.info:focus{outline:2px solid var(--blue)}.tip{display:none;position:absolute;z-index:5;left:0;top:23px;min-width:230px;max-width:290px;width:max-content;background:#273650;border:1px solid #526482;border-radius:8px;padding:10px 12px;color:#fff;font:normal 12px/1.5 system-ui;box-shadow:0 6px 25px #0008}.info:hover .tip,.info:focus .tip{display:block}.headrow{display:flex;align-items:center;justify-content:space-between;gap:10px;flex-wrap:wrap}.link{color:var(--blue);text-decoration:none;font-weight:700}.link:hover{text-decoration:underline}footer{margin-top:25px;color:var(--muted);font-size:12px}@media(max-width:850px){.grid{grid-template-columns:repeat(2,minmax(0,1fr))}.two{grid-template-columns:1fr}}@media(max-width:480px){main{padding:20px 12px}.section{padding:15px}.card{padding:12px}.value{font-size:24px}.tip{left:auto;right:-35px;min-width:190px}}
</style>
</head>
<body><main>
<header><h1>OpenMRS <span style="color:var(--blue)">QA Dashboard</span></h1><p class="sub">API + UI test automation metrics</p></header>
<section class="section"><h2>Release Readiness Status <span class="info" tabindex="0" role="button" aria-label="О готовности релиза">i<span class="tip">Готовность релиза определяется по результатам критических тестов, покрытию критических функций и результатам проверки CI.</span></span></h2><div class="status" id="releaseStatus">UNKNOWN</div><div id="reasons"></div></section>
<section class="section"><h2>Test Overview</h2><div class="grid" id="overview"></div></section>
<section class="section"><h2>Last Test Results</h2><div class="grid" id="results"></div><div id="executionBars" class="bars"></div><div id="executionLegend" class="legend"></div></section>
<section class="section"><h2>Historical Test Results</h2><div class="grid" id="historical"></div></section>
<section class="section"><h2>Critical Tests &amp; Feature Coverage</h2><div class="grid" id="critical"></div><div id="criticalBars" class="bars"></div><div id="criticalLegend" class="legend"></div></section>
<section class="section"><h2>API Coverage <span class="info" tabindex="0" role="button" aria-label="О покрытии API">i<span class="tip">Покрытие проверок Swagger Coverage. Показатель отражает долю покрытых условий из спецификации API, а не процент покрытых endpoint-ов.</span></span></h2><div class="grid" id="apiCoverage"></div></section>
<section class="section"><h2>Team Efficiency</h2><div class="grid" id="teamEfficiency"></div></section>
<footer id="footer"></footer>
</main><script>
'use strict';
const DATA=__METRICS_PAYLOAD__,inv=DATA.inventory,run=DATA.execution,rr=DATA.readiness;
const $=id=>document.getElementById(id);
const str=v=>v===null||v===undefined?'N/A':String(v);
const num=v=>Number.isFinite(Number(v))?Number(v):0;
const pct=v=>v===null||v===undefined?'N/A':Number(v).toFixed(1)+'%';
const duration=ms=>ms===null||ms===undefined?'N/A':`${Math.floor(ms/60000)}m ${Math.round((ms%60000)/1000)}s`;
function add(parent,tag,value,cls){const e=document.createElement(tag);e.textContent=str(value);if(cls)e.className=cls;parent.append(e);return e;}
function info(text){const i=document.createElement('span');i.className='info';i.tabIndex=0;i.setAttribute('role','button');i.setAttribute('aria-label','О метрике');i.textContent='i';add(i,'span',text,'tip');return i;}
function card(target,label,value,detail,description){const c=add($(target),'div','','card'),head=add(c,'div',label,'label');head.append(info(description));add(c,'div',value,'value');if(detail)add(c,'div',detail,'detail');}
const s=run.summary,i=inv.summary,c=rr.criticalTests;
const status=str(rr.status).toUpperCase();const badge=$('releaseStatus');badge.textContent=status.replaceAll('_',' ');badge.className='status '+(status==='READY'?'ready':status==='NOT_READY'?'not-ready':'unknown');
for(const reason of rr.reasons??[])add($('reasons'),'div',reason,'reason');for(const warning of rr.warnings??[])add($('reasons'),'div',warning,'reason warning');if(!(rr.reasons??[]).length)add($('reasons'),'p','No blocking reasons reported.','muted');
card('overview','Total Tests',i.total,'','Общее количество уникальных тестов в реестре тестов.');
card('overview','Active Tests',`${i.active} (${i.total>0?pct(i.active/i.total*100):'N/A'})`,'','Количество тестов, не отключённых в исходном коде. Процент от Total Tests.');
card('overview','Disabled Tests',`${i.disabled} (${i.total>0?pct(i.disabled/i.total*100):'N/A'})`,'','Количество тестов, отключённых в исходном коде. Процент от Total Tests.');
card('overview','API / UI',`${i.api} / ${i.ui}`,'API / UI','Распределение тестов по типам: API и UI.');
card('results','Pass Rate',pct(s.passRate),'','Доля успешно пройденных тестов среди выполненных тестов. Отключённые тесты не учитываются.');
card('results','Tests Passed',s.passed,'','Количество уникальных тестов, успешно пройденных в этом запуске.');
card('results','Tests Failed',s.failed,'','Количество тестов, завершившихся ошибкой проверки (assertion).');
card('results','Tests Broken',s.broken,'','Количество тестов, завершившихся ошибкой выполнения.');
card('results','Tests Skipped',s.skipped,'','Количество тестов, пропущенных во время выполнения.');
card('results','Tests Not Run',s.notRun,'','Количество активных тестов, для которых отсутствует результат выполнения.');
// Duration for the originating CI run, not the most recently completed unrelated run.
const history=(DATA.history??[]).filter(x=>x.workflowRunId&&x.workflowRunAttempt);
const latest=[...history].sort((a,b)=>String(b.completedAt??'').localeCompare(String(a.completedAt??'')));
const unique=[],seen=new Set();for(const h of latest){const key=String(h.workflowRunId)+'/'+String(h.workflowRunAttempt);if(!seen.has(key)){seen.add(key);unique.push(h);}}
const current=unique.find(h=>String(h.workflowRunId)===String(rr.workflowRunId)&&String(h.workflowRunAttempt)===String(rr.workflowRunAttempt));
const validDuration=h=>typeof h.testRunDurationMs==='number'&&Number.isFinite(h.testRunDurationMs)&&h.testRunDurationMs>=0;
card('results','Test Run Duration',duration(current&&validDuration(current)?current.testRunDurationMs:null),'','Продолжительность выполнения тестов в текущем CI-запуске.');
// Historical indicators: ten latest runs of the originating branch.
// Older duration records without a branch remain eligible for backward compatibility;
// pass-rate records must have a branch to avoid mixing different branches.
const flaky=DATA.flakyMetrics??{};
const branch=flaky.branch??current?.branch??null;
const scoped=unique.filter(h=>!branch||!h.branch||h.branch===branch).slice(0,10);
const rates=scoped.filter(h=>h.branch===branch&&typeof h.passRate==='number'&&Number.isFinite(h.passRate)&&h.passRate>=0&&h.passRate<=100);
const averagePassRate=rates.length?rates.reduce((sum,h)=>sum+h.passRate,0)/rates.length:null;
card('historical','Average Pass Rate',pct(averagePassRate),rates.length?`Last ${rates.length} CI run(s)`:'History unavailable','Среднее арифметическое Pass Rate за последние 10 CI-запусков этой ветки с сохранённым Pass Rate.');
const observed=Number(flaky.windowRuns??0),required=Number(flaky.minObservations??3);
const flakyDetail=flaky.flakyRate===null||flaky.flakyRate===undefined
  ?`Insufficient history (${observed}/${required} runs)`
  :`${flaky.flakyCandidates} / ${flaky.analyzedTests} tests · last ${observed} CI runs`;
card('historical','Flaky Rate',pct(flaky.flakyRate),flakyDetail,'History-based: доля тестов с успешными и неуспешными результатами за последние 10 запусков одной ветки. Минимум 3 наблюдения на тест.');
const durations=scoped.filter(validDuration);
const average=durations.length?durations.reduce((sum,h)=>sum+h.testRunDurationMs,0)/durations.length:null;
card('historical','Average Test Run Duration',duration(average),durations.length?`Last ${durations.length} CI run(s)`:'History unavailable','Средняя продолжительность выполнения тестов по последним 10 CI-запускам этой ветки с корректными измерениями.');
card('critical','Critical Tests',c.total,'','Общее количество тестов с уровнем критичности Critical.');
card('critical','Critical Tests Passed',c.passed,'','Количество критических тестов, успешно пройденных в этом запуске.');
const critExecuted=num(c.passed)+num(c.failedOrBroken);card('critical','Critical Tests Pass Rate',critExecuted?pct(100*num(c.passed)/critExecuted):'N/A','','Доля успешно пройденных критических тестов среди выполненных критических тестов.');
card('critical','Critical Tests Failed / Broken',c.failedOrBroken,'','Количество критических тестов, завершившихся ошибкой проверки или выполнения.');
card('critical','Critical Tests Disabled',c.disabled,'','Количество критических тестов, отключённых в исходном коде.');
const cov=rr.criticalFeatureCoverage;card('critical','Critical Feature Coverage',pct(cov.percent),`${cov.covered}/${cov.total} features`,'Доля критических функций, связанных хотя бы с одним автоматизированным тестом. Наличие теста не означает, что он успешно прошёл.');
function bars(containerId,legendId,segments){const total=segments.reduce((a,x)=>a+num(x[1]),0);for(const [name,count,color] of segments){const p=document.createElement('span');p.style.width=(total?100*num(count)/total:0)+'%';p.style.background=color;$(containerId).append(p);const item=add($(legendId),'span',`${name}: ${count}`);const dot=document.createElement('span');dot.className='dot';dot.style.background=color;item.prepend(dot);}}
bars('executionBars','executionLegend',[['Passed',s.passed,'#35d39c'],['Failed',s.failed,'#ff6c79'],['Broken',s.broken,'#d38bff'],['Skipped',s.skipped,'#f6c56b'],['Not run',s.notRun,'#74b8ff'],['Disabled',s.disabled,'#697a96']]);
bars('criticalBars','criticalLegend',[['Passed',c.passed,'#35d39c'],['Failed / broken',c.failedOrBroken,'#ff6c79'],['Skipped',c.skipped,'#f6c56b'],['Not run',c.notRun,'#74b8ff'],['Disabled',c.disabled,'#697a96']]);
const ac=DATA.apiCoverage??{};
card('apiCoverage','Covered Checks',ac.covered??'N/A','','Количество условий Swagger Coverage, покрытых выполненными API-тестами.');
card('apiCoverage','Total Checks',ac.all??'N/A','','Общее количество возможных проверок (условий) в отчёте Swagger Coverage.');
card('apiCoverage','API Coverage',pct(ac.coveragePercent),'','Доля покрытых условий: Covered Checks / Total Checks × 100%.');
card('apiCoverage','Minimum Target',pct(ac.minCoveragePercent),'','Целевой минимальный процент покрытия API. Сейчас это информационная цель, не блокирующая CI.');
const repair=DATA.repairMetrics??{}, pr=DATA.prMetrics??{};
const hours=minutes=>minutes===null||minutes===undefined||!Number.isFinite(Number(minutes))?'N/A':`${Math.floor(Number(minutes)/60)}h ${Math.round(Number(minutes)%60)}m`;
card('teamEfficiency','Average Test Repair Time',hours(repair.averageTestRepairMinutes),repair.windowDays?`Last ${repair.windowDays} days`:'History unavailable','От первого CI-обнаружения @Disabled(\"BUG: ...\") до успешного прохождения теста после удаления аннотации. Календарное время.');
card('teamEfficiency','Tests Under Repair',repair.testsUnderRepair??'N/A','','Количество тестов, у которых открыт цикл починки BUG.');
card('teamEfficiency','Repaired Tests',repair.repairedTests??'N/A',repair.windowDays?`Last ${repair.windowDays} days`:'History unavailable','Количество закрытых циклов починки за период.');
card('teamEfficiency','Average PR Approval Time',hours(pr.averagePrApprovalMinutes),pr.windowDays?`Last ${pr.windowDays} days`:'Data unavailable','Среднее время от готовности PR к ревью до первого одобрения человеком.');
card('teamEfficiency','PRs Awaiting Approval',pr.prsAwaitingApproval??'N/A','Current open PRs','Количество открытых не-Draft PR, ожидающих одобрения ревьюеров.');
card('teamEfficiency','PRs Ready To Merge',pr.prsReadyToMerge??'N/A','Current open PRs','Количество открытых не-Draft PR с действующим approval, без конфликтов и с выполненными обязательными проверками.');
$('footer').textContent=`Generated ${DATA.generatedAt} · Current run: ${str(rr.workflowRunId)} (attempt ${str(rr.workflowRunAttempt)})`;
</script></body></html>

""";
}
