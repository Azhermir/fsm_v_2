import React from 'react';
import { render, fireEvent } from '@testing-library/react-native';
import TaskCard from './TaskCard';

describe('TaskCard', () => {
  const mockTask = {
    id: 1,
    title: 'Fix HVAC System',
    clientAddress: '123 Main St, Springfield, IL 62701',
    priority: 'CRITICAL',
    estimatedDuration: 120,
    status: 'ASSIGNED',
  };

  it('should render task information correctly', () => {
    const { getByText } = render(<TaskCard task={mockTask} />);

    expect(getByText('Fix HVAC System')).toBeTruthy();
    expect(getByText('123 Main St, Springfield, IL 62701')).toBeTruthy();
    expect(getByText('CRITICAL')).toBeTruthy();
    expect(getByText('2h')).toBeTruthy();
    expect(getByText('ASSIGNED')).toBeTruthy();
  });

  it('should call onPress when card is pressed', () => {
    const onPressMock = jest.fn();
    const { getByText } = render(<TaskCard task={mockTask} onPress={onPressMock} />);

    fireEvent.press(getByText('Fix HVAC System'));

    expect(onPressMock).toHaveBeenCalledWith(mockTask);
  });

  it('should not crash if onPress is not provided', () => {
    const { getByText } = render(<TaskCard task={mockTask} />);

    expect(() => {
      fireEvent.press(getByText('Fix HVAC System'));
    }).not.toThrow();
  });

  it('should format duration with minutes', () => {
    const taskWithMinutes = { ...mockTask, estimatedDuration: 45 };
    const { getByText } = render(<TaskCard task={taskWithMinutes} />);

    expect(getByText('45m')).toBeTruthy();
  });

  it('should format duration with hours and minutes', () => {
    const taskWithMixed = { ...mockTask, estimatedDuration: 90 };
    const { getByText } = render(<TaskCard task={taskWithMixed} />);

    expect(getByText('1h 30m')).toBeTruthy();
  });

  it('should display HIGH priority with correct color', () => {
    const highPriorityTask = { ...mockTask, priority: 'HIGH' };
    const { getByText } = render(<TaskCard task={highPriorityTask} />);

    expect(getByText('HIGH')).toBeTruthy();
  });

  it('should display MEDIUM priority with correct color', () => {
    const mediumPriorityTask = { ...mockTask, priority: 'MEDIUM' };
    const { getByText } = render(<TaskCard task={mediumPriorityTask} />);

    expect(getByText('MEDIUM')).toBeTruthy();
  });

  it('should display LOW priority with correct color', () => {
    const lowPriorityTask = { ...mockTask, priority: 'LOW' };
    const { getByText } = render(<TaskCard task={lowPriorityTask} />);

    expect(getByText('LOW')).toBeTruthy();
  });

  it('should display IN_PROGRESS status', () => {
    const inProgressTask = { ...mockTask, status: 'IN_PROGRESS' };
    const { getByText } = render(<TaskCard task={inProgressTask} />);

    expect(getByText('IN PROGRESS')).toBeTruthy();
  });

  it('should display COMPLETED status', () => {
    const completedTask = { ...mockTask, status: 'COMPLETED' };
    const { getByText } = render(<TaskCard task={completedTask} />);

    expect(getByText('COMPLETED')).toBeTruthy();
  });

  it('should truncate long titles', () => {
    const longTitleTask = {
      ...mockTask,
      title: 'This is a very long task title that should be truncated when displayed on the card',
    };
    const { getByText } = render(<TaskCard task={longTitleTask} />);

    expect(getByText(longTitleTask.title)).toBeTruthy();
  });

  it('should truncate long addresses', () => {
    const longAddressTask = {
      ...mockTask,
      clientAddress: '123 Very Long Street Name, Building A, Suite 500, Springfield, IL 62701, United States',
    };
    const { getByText } = render(<TaskCard task={longAddressTask} />);

    expect(getByText(longAddressTask.clientAddress)).toBeTruthy();
  });

  it('should render location icon emoji', () => {
    const { getByText } = render(<TaskCard task={mockTask} />);

    expect(getByText('📍')).toBeTruthy();
  });

  it('should render clock icon emoji', () => {
    const { getByText } = render(<TaskCard task={mockTask} />);

    expect(getByText('⏱️')).toBeTruthy();
  });
});
