:warning: **More detailed information will be added in the future. This file needs to be updated.**

## CLI Usage
The packaged jar offers a CLI by [Picocli](https://picocli.info/) with the following features.

### Evaluation (Default)
<p>
Runs the pipeline and evaluates it.
</p>

#### Examples
`java -jar ./ratlr.jar eval` runs the pipeline using the default *./config.json* and evaluates it based on specified ground truth.<br>
`java -jar ./ratlr.jar eval -c ./configs` invokes the pipeline for each file inside *./configs* using them as configuration with specified ground truth.<br>
`java -jar ./ratlr.jar eval -c ./configs/simple.json ./configs/reasoning` specifying multiple configuration directories or files is possible.

### Evaluation (Transitive)
<p>
Runs the pipeline in transitive mode and evaluates it.
</p>

#### Examples
`java -jar ./ratlr.jar transitive -c ./example-configs/transitive/d2m.json ./example-configs/transitive/m2c.json -e ./example-configs/transitive/eval.json` runs the pipeline in transitive mode using the specified configurations and evaluates it based on specified ground truth.

First, the pipeline executes `d2m.json` and `m2c.json` configurations in order to create a two set of trace links.
Then, it connects the two sets of trace links and evaluates them based on the ground truth specified in `eval.json`.
