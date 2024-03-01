const util = require("node:util");
const exec = util.promisify(require("node:child_process").exec);

const main = async () => {
  const fossaResult = await fossaTest();
  console.info("finished!");
};

const fossaTest = async () => {
  // fossa test --diff 7f7fe24369a5413be51d95cd768bf35b12f1bff2
  // fossa analyze?
  const { stdout, stderr, code } = await cmd("fossa test --format json", { swallow: true });

  if (code !== 0 && !stdout) {
    console.error("!!! Error running FOSSA test:", { stderr, stdout, code });
    throw new Error(code);
  } else if (code !== 0) {
    console.error("!!! FOSSA found issues:\n", stderr, "\n\n!!!!!!");
  }

  try {
    const parsedOutput = JSON.parse(stdout || stderr);
    console.log("Parsed JSON output:", parsedOutput);
    return parsedOutput;
  } catch (parseError) {
    console.error("Error parsing FOSSA JSON:", parseError);
    throw new Error("Error parsing FOSSA JSON");
  }
};

// Run a shell command and return the result
const cmd = async (cmdStr, opts = { swallow: false }) => {
  try {
    const res = await exec(cmdStr); //.toString();
    return res;
  } catch (error) {
    if (opts.swallow) {
      return error;
    }
    throw error;
  }
};

main();

// gh actions build
// release trigger will publish to artifactory and wait
// artifactory triggers gh action webhook
// gh action will complete the release
