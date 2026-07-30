import { computed, reactive } from 'vue';

import { Cytomine } from '@/api';
import Task from '@/utils/appengine/task';

vi.mock('@/api', () => ({
  Cytomine: { instance: { api: { get: vi.fn() } } },
}));

// Issue 12 replaced `Vue.set(this, 'outputs', data)` here with plain assignment.
// Unlike TaskRun's fields, `outputs` is already declared in `_initProperties`,
// so this site was never the "add a key Vue cannot see" case `Vue.set` existed
// for — it was overwriting a property that was reactive all along.
describe('Task.fetchOutputs', () => {

  function task() {
    return reactive(new Task({ id: 1, namespace: 'ns', version: '1.0.0' }));
  }

  beforeEach(() => {
    Cytomine.instance.api.get.mockReset();
  });

  it('should publish the outputs reactively once fetched', async () => {
    const subject = task();
    const tracked = computed(() => subject.outputs);
    expect(tracked.value).toEqual([]);

    const payload = [{ name: 'out', type: { id: 'geometry' } }];
    Cytomine.instance.api.get.mockResolvedValueOnce({ data: payload });
    const returned = await subject.fetchOutputs();

    expect(returned).toEqual(payload);
    expect(tracked.value).toEqual(payload);
  });

  it('should request the outputs for its own namespace and version', async () => {
    const subject = task();
    Cytomine.instance.api.get.mockResolvedValueOnce({ data: [] });

    await subject.fetchOutputs();

    expect(Cytomine.instance.api.get).toHaveBeenCalledWith(
      expect.stringContaining('/ns/1.0.0/outputs')
    );
  });
});
