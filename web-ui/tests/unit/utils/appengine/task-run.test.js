import { computed, reactive } from 'vue';

import { Cytomine } from '@/api';
import TaskRun from '@/utils/appengine/task-run';

vi.mock('@/api', () => ({
  Cytomine: { instance: { api: { get: vi.fn() } } },
}));

describe('TaskRun lazily fetched fields', () => {

  function taskRun(overrides = {}) {
    return reactive(new TaskRun({
      id: 'run-1',
      project: 42,
      state: TaskRun.STATES.FINISHED,
      ...overrides,
    }));
  }

  beforeEach(() => {
    Cytomine.instance.api.get.mockReset();
  });

  it.each([
    ['inputs', 'fetchInputs', [{ name: 'a', value: 1 }]],
    ['outputs', 'fetchOutputs', [{ name: 'b', value: 2 }]],
    ['logs', 'fetchLogs', 'some log output'],
  ])('should publish %s reactively once fetched', async (field, method, payload) => {
    const run = taskRun();
    const tracked = computed(() => run[field]);
    expect(tracked.value).toBeUndefined();

    Cytomine.instance.api.get.mockResolvedValueOnce({ data: payload });
    const returned = await run[method]();

    expect(returned).toEqual(payload);
    expect(tracked.value).toEqual(payload);
  });

  it('should not fetch inputs for a run that has not started', async () => {
    const run = taskRun({ state: TaskRun.STATES.CREATED });

    expect(await run.fetchInputs()).toBeNull();
    expect(Cytomine.instance.api.get).not.toHaveBeenCalled();
    expect(run.inputs).toBeUndefined();
  });

  it.each(['fetchOutputs', 'fetchLogs'])(
    'should not %s for a run that has not finished', async method => {
      const run = taskRun({ state: TaskRun.STATES.RUNNING });

      expect(await run[method]()).toBeNull();
      expect(Cytomine.instance.api.get).not.toHaveBeenCalled();
    });
});
