import fetch from "node-fetch";

let passkeystring = "username=SECRET&password=SECRET";

function measurePromise(fn) {
  let onPromiseDone = () => performance.now() - start;

  let start = performance.now();
  return fn().then(onPromiseDone, onPromiseDone);
}

function getUssdPromise() {
  return fetch(
    "https://SECRET.pythonanywhere.com/ussd/?sessionid=SECRET&msisdn=255000000000&input=" +
      Math.round(Math.random() * 3)
  );
}

function getCallPromise() {
  return fetch(
    "https://SECRET.pythonanywhere.com/ussd/?msisdn=255000000000&input="
  );
}

function getAndroidDatePromise() {
  return fetch("https://SECRET.pythonanywhere.com/date/");
}

function getAndroidBusinessPromise() {
  return fetch("https://SECRET.pythonanywhere.com/business/", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: passkeystring,
  });
}

async function runTrials(calls, length) {
  let averageInitialCall = 0;
  let averageSession = 0;
  for (let c = 0; c < calls; c++) {
    duration = await measurePromise(() => getCallPromise());
    averageInitialCall += duration;
    for (let i = 0; i < length; i++) {
      duration += await measurePromise(() => getUssdPromise());
    }
    averageSession += duration;
  }
  return {
    averageInitialCall: averageInitialCall / calls,
    averageSession: averageSession / calls,
    averageRequest: averageSession / calls / length,
  };
}

async function runTrialsSimult(calls, length) {
  let callpromises = [];
  let ussdpromises = [];
  for (let c = 0; c < calls; c++) {
    callpromises.push(measurePromise(() => getCallPromise()));
    for (let i = 0; i < length; i++) {
      ussdpromises.push(measurePromise(() => getUssdPromise()));
    }
  }
  let averageInitialCall = 0;
  let averageSession = 0;
  for (let c = 0; c < calls; c++) {
    averageInitialCall += await callpromises[c];
    for (let i = 0; i < length; i++) {
      averageSession += await ussdpromises.pop();
    }
  }
  return {
    averageInitialCall: averageInitialCall / calls,
    averageSession: averageSession / calls,
    averageRequest: averageSession / calls / length,
  };
}

async function runAndroidTrials(trials) {
  let dateduration = 0;
  let businessduration = 0;
  for (let t = 0; t < trials; t++) {
    dateduration += await measurePromise(() => getAndroidDatePromise());
    businessduration += await measurePromise(() => getAndroidBusinessPromise());
  }
  return {
    averageDateRequest: dateduration / trials,
    averageBusinessRequest: businessduration / trials,
    averageBothRequests: (dateduration + businessduration) / trials,
  };
}

async function runAndroidTrialsSimult(trials) {
  let datepromises = [];
  let businesspromises = [];
  for (let t = 0; t < trials; t++) {
    datepromises.push(measurePromise(() => getAndroidDatePromise()));
    businesspromises.push(measurePromise(() => getAndroidBusinessPromise()));
  }
  let dateduration = 0;
  let businessduration = 0;
  for (let t = 0; t < trials; t++) {
    dateduration += await datepromises.pop();
    businessduration += await businesspromises.pop();
  }
  return {
    averageDateRequest: dateduration / trials,
    averageBusinessRequest: businessduration / trials,
    averageBothRequests: (dateduration + businessduration) / trials,
  };
}

// await runTrialsSimult(10,10).then((d)=> console.log("10 w/ 10 => " + JSON.stringify(d)));
// await runTrialsSimult(10,100).then((d)=>console.log("10 w/ 100 =>" + JSON.stringify(d)));

// await runTrialsSimult(1, 10).then((d)=>console.log("1. 1 w/ 10 =>" + JSON.stringify(d)));
// await runTrialsSimult(1, 100).then((d)=>console.log("2. 1 w/ 100 =>" + JSON.stringify(d)));
// await runTrialsSimult(1, 1000).then((d)=>console.log("3. 1 w/ 1000 =>" + JSON.stringify(d)));
// await runTrialsSimult(1, 2000).then((d)=>console.log("4. 1 w/ 2000 =>" + JSON.stringify(d)));
// await runTrialsSimult(1, 3000).then((d)=>console.log("5. 1 w/ 3000 =>" + JSON.stringify(d)));
// await runTrialsSimult(1, 4000).then((d)=>console.log("6. 1 w/ 4000 =>" + JSON.stringify(d)));
// await runTrialsSimult(1, 5000).then((d)=>console.log("7. 1 w/ 5000 =>" + JSON.stringify(d)));
// await runTrialsSimult(1, 6000).then((d)=>console.log("8. 1 w/ 6000 =>" + JSON.stringify(d)));
// await runTrialsSimult(1, 7000).then((d)=>console.log("9. 1 w/ 7000 =>" + JSON.stringify(d)));
// await runTrialsSimult(1, 10000).then((d)=>console.log("10. 1 w/ 10000 =>" + JSON.stringify(d)));

// await runAndroidTrialsSimult(10).then((d)=> console.log("10 => " + JSON.stringify(d)));
// await runAndroidTrialsSimult(100).then((d)=> console.log("100 => " + JSON.stringify(d)));
// await runAndroidTrialsSimult(1000).then((d)=> console.log("1000 => " + JSON.stringify(d)));
// await runAndroidTrialsSimult(2000).then((d)=> console.log("2000 => " + JSON.stringify(d)));
// await runAndroidTrialsSimult(3000).then((d)=> console.log("3000 => " + JSON.stringify(d)));
// await runAndroidTrialsSimult(4000).then((d)=> console.log("4000 => " + JSON.stringify(d)));
// await runAndroidTrialsSimult(5000).then((d)=> console.log("5000 => " + JSON.stringify(d)));
// await runAndroidTrialsSimult(6000).then((d)=> console.log("6000 => " + JSON.stringify(d)));
// await runAndroidTrialsSimult(7000).then((d)=> console.log("7000 => " + JSON.stringify(d)));
